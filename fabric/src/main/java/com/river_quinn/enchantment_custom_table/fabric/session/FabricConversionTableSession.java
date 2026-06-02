package com.river_quinn.enchantment_custom_table.fabric.session;

import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.platform.TableConfigService;
import com.river_quinn.enchantment_custom_table.core.session.GeneratedSlotPage;
import com.river_quinn.enchantment_custom_table.core.session.TableOperationResult;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class FabricConversionTableSession {
    private final Level world;
    private final LogicalInventory inventory;
    private final BooleanSupplier copyMode;
    private final TableConfigService config;
    private final int bookSlot;
    private final int paymentSlot;
    private final int generatedSlotStart;
    private final int generatedSlotCount;
    private final List<Holder<Enchantment>> allEnchantments = new ArrayList<>();
    private String searchQuery = "";
    private String searchClientLanguage = "";
    private boolean usingClientSearchMatches = false;
    private Set<String> clientMatchedEnchantments = Set.of();
    private int currentPage = 0;
    private int totalPage = 0;

    public FabricConversionTableSession(
            Level world,
            LogicalInventory inventory,
            BooleanSupplier copyMode,
            TableConfigService config,
            int bookSlot,
            int paymentSlot,
            int generatedSlotStart,
            int generatedSlotCount
    ) {
        this.world = world;
        this.inventory = inventory;
        this.copyMode = copyMode;
        this.config = config;
        this.bookSlot = bookSlot;
        this.paymentSlot = paymentSlot;
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

    public void setSearchQuery(String query) {
        setSearchQuery(query, "", List.of());
    }

    public void setSearchQuery(String query, String clientLanguage, List<String> matchedEnchantments) {
        searchQuery = EnchantmentSearchRules.sanitizeSearchQuery(query);
        searchClientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
        usingClientSearchMatches = !EnchantmentSearchRules.isBlankSearch(searchQuery) && matchedEnchantments != null;
        if (usingClientSearchMatches) {
            clientMatchedEnchantments = matchedEnchantments.stream()
                    .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                    .collect(HashSet::new, Set::add, Set::addAll);
        } else {
            clientMatchedEnchantments = Set.of();
        }
        regenerateGeneratedSlots();
    }

    public String searchQuery() {
        return searchQuery;
    }

    public String searchClientLanguage() {
        return searchClientLanguage;
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

    public void turnPage(int page) {
        if (page < 0 || page >= totalPage) {
            return;
        }
        currentPage = page;
        clearGeneratedSlots();
        generateGeneratedSlots();
    }

    public void regenerateGeneratedSlots() {
        if (copyMode.getAsBoolean()) {
            resetPage();
            clearGeneratedSlots();
            return;
        }
        List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
        currentPage = 0;
        totalPage = EnchantmentTableRules.calculatePageCount(enchantments.size(), generatedSlotCount, false);
        clearGeneratedSlots();
        generateGeneratedSlots();
    }

    public boolean canPickGeneratedBook() {
        return !copyMode.getAsBoolean()
                && inventory.getStackInSlot(bookSlot).is(Items.BOOK)
                && EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config.snapshot());
    }

    public TableOperationResult pickGeneratedBook() {
        if (!canPickGeneratedBook()) {
            clearGeneratedSlots();
            return TableOperationResult.failed(true);
        }
        inventory.getStackInSlot(bookSlot).shrink(1);
        EnchantmentTableRules.consumePayment(inventory.getStackInSlot(paymentSlot), config.snapshot());
        generateGeneratedSlots();
        return TableOperationResult.success(true);
    }

    public static TableOperationResult refreshCopyResult(
            LogicalInventory inventory,
            BooleanSupplier copyMode,
            TableConfigService config,
            int bookSlot,
            int paymentSlot,
            int templateSlot,
            int copyResultSlot
    ) {
        if (!EnchantmentTableRules.shouldGenerateCopyResult(
                copyMode.getAsBoolean(),
                inventory.getStackInSlot(copyResultSlot).isEmpty(),
                inventory.getStackInSlot(bookSlot).is(Items.BOOK)
                        && EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config.snapshot())
        )) {
            return TableOperationResult.failed(false);
        }
        inventory.getStackInSlot(bookSlot).shrink(1);
        EnchantmentTableRules.consumePayment(inventory.getStackInSlot(paymentSlot), config.snapshot());
        inventory.setStackInSlot(copyResultSlot, inventory.getStackInSlot(templateSlot).copyWithCount(1));
        return TableOperationResult.success(true);
    }

    private void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    private void clearGeneratedSlots() {
        for (int i = 0; i < generatedSlotCount; i++) {
            inventory.setStackInSlot(generatedSlotStart + i, ItemStack.EMPTY);
        }
    }

    private void generateGeneratedSlots() {
        if (copyMode.getAsBoolean()) {
            resetPage();
            clearGeneratedSlots();
            return;
        }

        List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
        if (!inventory.getStackInSlot(bookSlot).is(Items.BOOK)
                || !EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config.snapshot())) {
            resetPage();
            clearGeneratedSlots();
            return;
        }

        for (int i = 0; i < generatedSlotCount; i++) {
            int slotIndex = i + generatedSlotStart;
            int enchantmentIndex = i + currentPage * generatedSlotCount;
            if (enchantmentIndex < enchantments.size()) {
                if (inventory.getStackInSlot(slotIndex).isEmpty()) {
                    Holder<Enchantment> enchantment = enchantments.get(enchantmentIndex);
                    inventory.setStackInSlot(slotIndex, createEnchantedBook(enchantment));
                }
            } else {
                inventory.setStackInSlot(slotIndex, ItemStack.EMPTY);
            }
        }
    }

    private void loadAllEnchantments() {
        if (allEnchantments.isEmpty()) {
            Registry<Enchantment> fullEnchantmentList = FabricVersionedMinecraft.enchantmentRegistry(world);
            fullEnchantmentList.asHolderIdMap().forEach(allEnchantments::add);
        }
    }

    private ItemStack createEnchantedBook(Holder<Enchantment> enchantment) {
        int enchantmentLevel = config.snapshot().convertOnlyLevelOneBook() ? 1 : enchantment.value().getMaxLevel();
        return FabricEnchantmentUtils.createEnchantedBook(enchantment, enchantmentLevel);
    }

    private List<Holder<Enchantment>> getFilteredEnchantments() {
        loadAllEnchantments();
        if (EnchantmentSearchRules.isBlankSearch(searchQuery)) {
            return allEnchantments;
        }
        return allEnchantments.stream().filter(this::matchesSearch).toList();
    }

    private boolean matchesSearch(Holder<Enchantment> enchantment) {
        Optional<ResourceKey<Enchantment>> enchantmentId = FabricEnchantmentUtils.getEnchantmentKey(world, enchantment);
        if (enchantmentId.isPresent() && usingClientSearchMatches && clientMatchedEnchantments.contains(FabricVersionedMinecraft.keyId(enchantmentId.get()))) {
            return true;
        }
        List<String> serverCandidates = new ArrayList<>();
        enchantmentId.ifPresent(id -> {
            serverCandidates.add(FabricVersionedMinecraft.keyId(id));
            serverCandidates.add(FabricVersionedMinecraft.keyNamespace(id));
            serverCandidates.add(FabricVersionedMinecraft.keyPath(id));
            serverCandidates.add(FabricVersionedMinecraft.keyPath(id).replace('_', ' '));
            serverCandidates.add("enchantment." + FabricVersionedMinecraft.keyNamespace(id) + "." + FabricVersionedMinecraft.keyPath(id));
        });
        serverCandidates.add(enchantment.value().description().getString());
        return EnchantmentSearchRules.matchesAnyCandidate(searchQuery, serverCandidates);
    }
}
