package com.river_quinn.enchantment_custom_table.fabric.session;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.session.GeneratedSlotPage;
import com.river_quinn.enchantment_custom_table.core.session.TableOperationResult;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FabricEnchantingTableSession {
    private final Level world;
    private final LogicalInventory inventory;
    private final Supplier<EnchantmentTableRules.MergeOptions> mergeOptions;
    private final int toolSlot;
    private final int inputSlot;
    private final int generatedSlotStart;
    private final int generatedSlotCount;
    private final List<ItemStack> generatedItems = new ArrayList<>();
    private int currentPage = 0;
    private int totalPage = 0;

    public record GeneratedBookRemovalResult(boolean success, boolean regenerated) {
        public static GeneratedBookRemovalResult failed() {
            return new GeneratedBookRemovalResult(false, false);
        }

        public static GeneratedBookRemovalResult success(boolean regenerated) {
            return new GeneratedBookRemovalResult(true, regenerated);
        }
    }

    public record ExportEnchantmentsResult(boolean success, ItemStack exportedStack) {
        public static ExportEnchantmentsResult failed() {
            return new ExportEnchantmentsResult(false, ItemStack.EMPTY);
        }

        public static ExportEnchantmentsResult success(ItemStack exportedStack) {
            return new ExportEnchantmentsResult(true, exportedStack);
        }
    }

    public FabricEnchantingTableSession(
            Level world,
            LogicalInventory inventory,
            Supplier<EnchantmentTableRules.MergeOptions> mergeOptions,
            int toolSlot,
            int inputSlot,
            int generatedSlotStart,
            int generatedSlotCount
    ) {
        this.world = world;
        this.inventory = inventory;
        this.mergeOptions = mergeOptions;
        this.toolSlot = toolSlot;
        this.inputSlot = inputSlot;
        this.generatedSlotStart = generatedSlotStart;
        this.generatedSlotCount = generatedSlotCount;
    }

    public GeneratedSlotPage page() {
        return new GeneratedSlotPage(currentPage, totalPage);
    }

    public int currentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int totalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public void nextPage() {
        if (currentPage < totalPage - 1) {
            turnPage(currentPage + 1);
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            turnPage(currentPage - 1);
        }
    }

    public void turnPage(int targetPage) {
        if (targetPage < 0 || targetPage >= totalPage) {
            return;
        }
        saveCurrentPageSlots();
        currentPage = targetPage;
        updateGeneratedSlots();
    }

    public void refreshGeneratedSlotsFromTool() {
        clearGeneratedSlots();
        generateCache();
        currentPage = totalPage == 0 ? 0 : Math.max(0, Math.min(currentPage, totalPage - 1));
        updateGeneratedSlots();
    }

    public void clearAll() {
        clearGeneratedSlots();
        generatedItems.clear();
        currentPage = 0;
        totalPage = 0;
    }

    public boolean canApplyEnchantedBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return false;
        }
        return EnchantmentTableRules.tryMergeEnchantments(
                FabricEnchantmentUtils.getEnchantments(toolItemStack),
                FabricEnchantmentUtils.getEnchantmentLevels(world, stack),
                enchantment -> FabricEnchantmentUtils.getCoreEnchantmentKey(world, enchantment),
                mergeOptions.get()
        ).allowed();
    }

    public TableOperationResult applyEnchantedBook(ItemStack stack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = FabricEnchantmentUtils.getEnchantmentLevels(world, stack);
        if (enchantmentLevels.isEmpty()) {
            return TableOperationResult.failed(false);
        }
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return TableOperationResult.failed(false);
        }

        EnchantmentTableRules.MergeResult result = EnchantmentTableRules.tryMergeEnchantments(
                FabricEnchantmentUtils.getEnchantments(toolItemStack),
                enchantmentLevels,
                enchantment -> FabricEnchantmentUtils.getCoreEnchantmentKey(world, enchantment),
                mergeOptions.get()
        );
        if (!result.allowed() || !replaceToolEnchantments(result.enchantments())) {
            return TableOperationResult.failed(false);
        }

        refreshGeneratedSlotsFromTool();
        return TableOperationResult.success(true);
    }

    public GeneratedBookRemovalResult removeGeneratedBook(ItemStack stack) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = FabricEnchantmentUtils.getEnchantmentLevels(world, stack);
        if (enchantmentLevels.isEmpty()) {
            return GeneratedBookRemovalResult.failed();
        }

        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return GeneratedBookRemovalResult.failed();
        }

        ItemEnchantments itemEnchantments = FabricEnchantmentUtils.getEnchantments(toolItemStack);
        ItemEnchantments resultEnchantments = EnchantmentTableRules.subtractEnchantments(
                itemEnchantments,
                enchantmentLevels,
                enchantment -> FabricEnchantmentUtils.getCoreEnchantmentKey(world, enchantment),
                mergeOptions.get().incrementalSameLevelMerge()
                        && toolItemStack.is(Items.ENCHANTED_BOOK)
                        && itemEnchantments.size() == 1
                        && enchantmentLevels.size() == 1
        );
        if (!replaceToolEnchantments(resultEnchantments)) {
            return GeneratedBookRemovalResult.failed();
        }

        boolean shouldRegenerate = (toolItemStack.is(Items.ENCHANTED_BOOK) && resultEnchantments.size() == 1)
                || totalPage != EnchantmentTableRules.calculatePageCount(resultEnchantments.size(), generatedSlotCount, true);
        if (shouldRegenerate) {
            refreshGeneratedSlotsFromTool();
        } else {
            updateGeneratedSlots();
        }
        return GeneratedBookRemovalResult.success(shouldRegenerate);
    }

    public ExportEnchantmentsResult exportAllEnchantments() {
        if (world.isClientSide()) {
            return ExportEnchantmentsResult.failed();
        }

        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        ItemEnchantments itemEnchantments = FabricEnchantmentUtils.getEnchantments(toolItemStack);
        if (toolItemStack.is(Items.ENCHANTED_BOOK)) {
            inventory.setStackInSlot(toolSlot, ItemStack.EMPTY);
            clearAll();
            return ExportEnchantmentsResult.success(toolItemStack.copy());
        }
        if (!toolItemStack.isEmpty() && !itemEnchantments.isEmpty()) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Holder<Enchantment> enchantment = FabricEnchantmentUtils.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
                mutable.set(enchantment, 0);
                enchantedBook.enchant(enchantment, entry.getIntValue());
            }
            if (!replaceToolEnchantments(mutable.toImmutable())) {
                return ExportEnchantmentsResult.failed();
            }
            clearAll();
            return ExportEnchantmentsResult.success(enchantedBook);
        }

        clearAll();
        return ExportEnchantmentsResult.failed();
    }

    private void generateCache() {
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        int currentTotalPage = 1;
        generatedItems.clear();

        if (!toolItemStack.isEmpty()) {
            ItemEnchantments enchantments = FabricEnchantmentUtils.getEnchantments(toolItemStack);
            currentTotalPage = EnchantmentTableRules.calculatePageCount(enchantments.entrySet().size(), generatedSlotCount, true);
            if (toolItemStack.is(Items.ENCHANTED_BOOK) && enchantments.entrySet().size() == 1) {
                Object2IntMap.Entry<Holder<Enchantment>> enchantmentObj = enchantments.entrySet().iterator().next();
                Holder<Enchantment> enchantment = FabricEnchantmentUtils.resolveEnchantmentHolder(world, enchantmentObj.getKey()).orElse(enchantmentObj.getKey());
                int enchantmentLevel = enchantmentObj.getIntValue();
                if (enchantmentLevel > 1) {
                    for (Integer level : EnchantmentTableRules.splitSingleEnchantmentLevels(enchantmentLevel, mergeOptions.get())) {
                        generatedItems.add(FabricEnchantmentUtils.createEnchantedBook(enchantment, level));
                    }
                }
            } else {
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                    Holder<Enchantment> enchantment = FabricEnchantmentUtils.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
                    generatedItems.add(FabricEnchantmentUtils.createEnchantedBook(enchantment, entry.getIntValue()));
                }
            }
        } else {
            currentTotalPage = 0;
        }

        int totalSlots = currentTotalPage * generatedSlotCount;
        while (generatedItems.size() < totalSlots) {
            generatedItems.add(ItemStack.EMPTY);
        }
        totalPage = currentTotalPage;
    }

    private void updateGeneratedSlots() {
        inventory.setStackInSlot(inputSlot, ItemStack.EMPTY);
        int indexOffset = currentPage * generatedSlotCount;
        for (int i = 0; i < generatedSlotCount; i++) {
            int indexOfFullList = i + indexOffset;
            inventory.setStackInSlot(
                    i + generatedSlotStart,
                    indexOfFullList < generatedItems.size() ? generatedItems.get(indexOfFullList) : ItemStack.EMPTY
            );
        }
    }

    private void saveCurrentPageSlots() {
        int indexOffset = currentPage * generatedSlotCount;
        for (int i = 0; i < generatedSlotCount; i++) {
            int indexOfFullList = i + indexOffset;
            if (indexOfFullList < generatedItems.size()) {
                generatedItems.set(indexOfFullList, inventory.getStackInSlot(i + generatedSlotStart));
            }
        }
    }

    private void clearGeneratedSlots() {
        for (int i = generatedSlotStart; i < generatedSlotStart + generatedSlotCount; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private boolean replaceToolEnchantments(ItemEnchantments enchantments) {
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return false;
        }
        EnchantmentHelper.setEnchantments(toolItemStack, enchantments);
        inventory.setStackInSlot(toolSlot, toolItemStack);
        return true;
    }
}
