package com.river_quinn.enchantment_custom_table.core.session;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EnchantingTableSession {
    private final Level world;
    private final LogicalInventory inventory;
    private final EnchantmentAccessService enchantments;
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

    public record ExportEnchantmentsResult(boolean success, boolean playSound, ItemStack exportedStack) {
        public static ExportEnchantmentsResult failed() {
            return new ExportEnchantmentsResult(false, false, ItemStack.EMPTY);
        }

        public static ExportEnchantmentsResult success(ItemStack exportedStack) {
            return new ExportEnchantmentsResult(true, true, exportedStack);
        }
    }

    public EnchantingTableSession(
            Level world,
            LogicalInventory inventory,
            EnchantmentAccessService enchantments,
            Supplier<EnchantmentTableRules.MergeOptions> mergeOptions,
            int toolSlot,
            int inputSlot,
            int generatedSlotStart,
            int generatedSlotCount
    ) {
        this.world = world;
        this.inventory = inventory;
        this.enchantments = enchantments;
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

    public int cacheIndexForGeneratedSlot(int slotIndex) {
        return EnchantmentTableRules.cacheIndexForGeneratedSlot(
                slotIndex,
                generatedSlotStart,
                currentPage,
                generatedSlotCount
        );
    }

    public int generatedItemCount() {
        return generatedItems.size();
    }

    public void setGeneratedItem(int index, ItemStack stack) {
        if (index >= 0 && index < generatedItems.size()) {
            generatedItems.set(index, stack.copy());
        }
    }

    public boolean isGeneratedItemAt(int index, ItemStack stack) {
        return index >= 0
                && index < generatedItems.size()
                && !stack.isEmpty()
                && ItemStack.isSameItemSameComponents(generatedItems.get(index), stack);
    }

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void nextPage() {
        if (currentPage < (totalPage - 1)) {
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

    public void updateGeneratedSlots() {
        inventory.setStackInSlot(inputSlot, ItemStack.EMPTY.copy());

        int indexOffset = currentPage * generatedSlotCount;
        if (totalPage > 0) {
            for (int i = 0; i < generatedSlotCount; i++) {
                int indexOfFullList = i + indexOffset;
                int indexOfSlot = i + generatedSlotStart;
                inventory.setStackInSlot(
                        indexOfSlot,
                        indexOfFullList < generatedItems.size()
                                ? generatedItems.get(indexOfFullList).copy()
                                : ItemStack.EMPTY
                );
            }
        }
    }

    public void clearCache() {
        clearGeneratedSlots();
        generateCache();
    }

    public void clearPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void clearAll() {
        clearGeneratedSlots();
        generatedItems.clear();
        resetPage();
    }

    public void generateCache() {
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);

        int currentTotalPage = 1;
        generatedItems.clear();

        if (!toolItemStack.isEmpty()) {
            ItemEnchantments itemEnchantments = enchantments.getEnchantments(toolItemStack);
            currentTotalPage = EnchantmentTableRules.calculatePageCount(itemEnchantments.entrySet().size(), generatedSlotCount, true);

            if (toolItemStack.is(Items.ENCHANTED_BOOK) && itemEnchantments.entrySet().size() == 1) {
                Object2IntMap.Entry<Holder<Enchantment>> enchantmentObj = itemEnchantments.entrySet().iterator().next();
                Holder<Enchantment> enchantment = enchantments.resolveEnchantmentHolder(world, enchantmentObj.getKey()).orElse(enchantmentObj.getKey());
                int enchantmentLevel = enchantmentObj.getIntValue();
                if (enchantmentLevel > 1) {
                    for (Integer level : EnchantmentTableRules.splitSingleEnchantmentLevels(enchantmentLevel, mergeOptions.get())) {
                        generatedItems.add(enchantments.createEnchantedBook(enchantment, level));
                    }
                }
            } else if (!toolItemStack.is(Items.ENCHANTED_BOOK) || itemEnchantments.entrySet().size() > 1) {
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                    Holder<Enchantment> enchantment = enchantments.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
                    int enchantmentLevel = entry.getIntValue();
                    generatedItems.add(enchantments.createEnchantedBook(enchantment, enchantmentLevel));
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

    public void refreshGeneratedSlotsFromTool() {
        clearGeneratedSlots();
        generateCache();
        currentPage = totalPage == 0 ? 0 : Math.max(0, Math.min(currentPage, totalPage - 1));
        updateGeneratedSlots();
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
                enchantments.getEnchantments(toolItemStack),
                enchantments.getEnchantmentLevels(world, stack),
                enchantment -> enchantments.getCoreEnchantmentKey(world, enchantment),
                mergeOptions.get()
        ).allowed();
    }

    public TableOperationResult applyEnchantedBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) {
            return TableOperationResult.failed(false);
        }
        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = enchantments.getEnchantmentLevels(world, stack);
        if (enchantmentLevels.isEmpty()) {
            return TableOperationResult.failed(false);
        }

        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return TableOperationResult.failed(false);
        }

        EnchantmentTableRules.MergeResult result = EnchantmentTableRules.tryMergeEnchantments(
                enchantments.getEnchantments(toolItemStack),
                enchantmentLevels,
                enchantment -> enchantments.getCoreEnchantmentKey(world, enchantment),
                mergeOptions.get()
        );
        if (!result.allowed() || !replaceToolEnchantments(result.enchantments())) {
            return TableOperationResult.failed(false);
        }

        generateCache();
        updateGeneratedSlots();
        return TableOperationResult.success(true);
    }

    public GeneratedBookRemovalResult removeGeneratedBook(ItemStack stack) {
        return removeGeneratedBook(stack, -1);
    }

    public GeneratedBookRemovalResult removeGeneratedBookAtCacheIndex(ItemStack stack, int cacheIndex) {
        if (!isGeneratedItemAt(cacheIndex, stack)) {
            return GeneratedBookRemovalResult.failed();
        }
        return removeGeneratedBook(stack, cacheIndex);
    }

    private GeneratedBookRemovalResult removeGeneratedBook(ItemStack stack, int cacheIndex) {
        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = enchantments.getEnchantmentLevels(world, stack);
        if (enchantmentLevels.isEmpty()) {
            return GeneratedBookRemovalResult.failed();
        }

        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return GeneratedBookRemovalResult.failed();
        }

        ItemEnchantments itemEnchantments = enchantments.getEnchantments(toolItemStack);
        if (!canRemoveEnchantments(itemEnchantments, enchantmentLevels)) {
            return GeneratedBookRemovalResult.failed();
        }

        ItemEnchantments resultEnchantments = EnchantmentTableRules.subtractEnchantments(
                itemEnchantments,
                enchantmentLevels,
                enchantment -> enchantments.getCoreEnchantmentKey(world, enchantment),
                shouldUseIncrementalSingleBookSplitRemoval(toolItemStack, itemEnchantments, enchantmentLevels)
        );
        if (!replaceToolEnchantments(resultEnchantments)) {
            return GeneratedBookRemovalResult.failed();
        }

        int resultPageSize = EnchantmentTableRules.calculatePageCount(
                resultEnchantments.size(),
                generatedSlotCount,
                true
        );
        boolean shouldRegenerate = (toolItemStack.is(Items.ENCHANTED_BOOK) && resultEnchantments.size() == 1)
                || totalPage != resultPageSize;
        if (shouldRegenerate) {
            refreshGeneratedSlotsFromTool();
        } else {
            setGeneratedItem(cacheIndex, ItemStack.EMPTY);
            updateGeneratedSlots();
        }
        return GeneratedBookRemovalResult.success(shouldRegenerate);
    }

    public ExportEnchantmentsResult exportAllEnchantments() {
        if (world.isClientSide()) {
            return ExportEnchantmentsResult.failed();
        }

        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        ItemEnchantments itemEnchantments = enchantments.getEnchantments(toolItemStack);
        if (toolItemStack.is(Items.ENCHANTED_BOOK)) {
            inventory.setStackInSlot(toolSlot, ItemStack.EMPTY);
            clearCache();
            clearPage();
            return ExportEnchantmentsResult.success(toolItemStack.copy());
        }
        if (!toolItemStack.isEmpty() && !itemEnchantments.isEmpty()) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                Holder<Enchantment> enchantment = enchantments.resolveEnchantmentHolder(world, entry.getKey()).orElse(entry.getKey());
                int enchantmentLevel = entry.getIntValue();

                mutable.set(enchantment, 0);
                enchantedBook.enchant(enchantment, enchantmentLevel);
            }

            if (!replaceToolEnchantments(mutable.toImmutable())) {
                return ExportEnchantmentsResult.failed();
            }
            clearCache();
            clearPage();
            return ExportEnchantmentsResult.success(enchantedBook);
        }

        clearCache();
        clearPage();
        return ExportEnchantmentsResult.failed();
    }

    private void saveCurrentPageSlots() {
        int indexOffset = currentPage * generatedSlotCount;
        for (int i = 0; i < generatedSlotCount; i++) {
            int indexOfFullList = i + indexOffset;
            int indexOfSlot = i + generatedSlotStart;
            if (indexOfFullList < generatedItems.size()) {
                generatedItems.set(indexOfFullList, inventory.getStackInSlot(indexOfSlot).copy());
            }
        }
    }

    private void clearGeneratedSlots() {
        for (int i = generatedSlotStart; i < generatedSlotStart + generatedSlotCount; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private boolean canRemoveEnchantments(
            ItemEnchantments itemEnchantments,
            List<EnchantmentTableRules.EnchantmentLevel> removalEnchantments
    ) {
        for (EnchantmentTableRules.EnchantmentLevel removal : removalEnchantments) {
            int currentLevel = EnchantmentTableRules.getMatchingEnchantmentLevel(
                    itemEnchantments,
                    removal.key(),
                    enchantment -> enchantments.getCoreEnchantmentKey(world, enchantment)
            );
            if (currentLevel <= 0 || currentLevel < removal.level()) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldUseIncrementalSingleBookSplitRemoval(
            ItemStack toolItemStack,
            ItemEnchantments itemEnchantments,
            List<EnchantmentTableRules.EnchantmentLevel> removalEnchantments
    ) {
        return mergeOptions.get().incrementalSameLevelMerge()
                && toolItemStack.is(Items.ENCHANTED_BOOK)
                && itemEnchantments.size() == 1
                && removalEnchantments.size() == 1;
    }

    private boolean replaceToolEnchantments(ItemEnchantments enchantments) {
        ItemStack toolItemStack = inventory.getStackInSlot(toolSlot);
        if (toolItemStack.isEmpty()) {
            return false;
        }
        this.enchantments.setEnchantments(toolItemStack, enchantments);
        inventory.setStackInSlot(toolSlot, toolItemStack);
        return true;
    }
}
