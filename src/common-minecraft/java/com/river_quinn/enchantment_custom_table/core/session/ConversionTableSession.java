package com.river_quinn.enchantment_custom_table.core.session;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.core.platform.TableConfigService;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class ConversionTableSession {
    private final Level world;
    private final LogicalInventory inventory;
    private final EnchantmentAccessService enchantments;
    private final BooleanSupplier copyMode;
    private final TableConfigService config;
    private final int bookSlot;
    private final int paymentSlot;
    private final int generatedSlotStart;
    private final int generatedSlotCount;

    private String searchQuery = "";
    private String searchClientLanguage = "";
    private boolean usingClientSearchMatches = false;
    private Set<String> clientMatchedEnchantments = Set.of();
    private int currentPage = 0;
    private int totalPage = 0;

    public ConversionTableSession(
            Level world,
            LogicalInventory inventory,
            EnchantmentAccessService enchantments,
            BooleanSupplier copyMode,
            TableConfigService config,
            int bookSlot,
            int paymentSlot,
            int generatedSlotStart,
            int generatedSlotCount
    ) {
        this.world = world;
        this.inventory = inventory;
        this.enchantments = enchantments;
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
        if (currentPage < (totalPage - 1)) {
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

    public void resetPage() {
        currentPage = 0;
        totalPage = 0;
    }

    public void clearGeneratedSlots() {
        for (int i = 0; i < generatedSlotCount; i++) {
            inventory.setStackInSlot(generatedSlotStart + i, ItemStack.EMPTY);
        }
    }

    public void generateGeneratedSlots() {
        if (copyMode.getAsBoolean()) {
            resetPage();
            clearGeneratedSlots();
            return;
        }

        List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
        boolean hasBook = inventory.getStackInSlot(bookSlot).is(Items.BOOK);
        boolean hasEnoughPayment = hasEnoughPayment();

        if (!hasBook || !hasEnoughPayment) {
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

    public void refreshGeneratedSlotsAfterInputsChanged() {
        if (copyMode.getAsBoolean()) {
            resetPage();
            clearGeneratedSlots();
            return;
        }

        if (!inventory.getStackInSlot(bookSlot).is(Items.BOOK) || !hasEnoughPayment()) {
            resetPage();
            clearGeneratedSlots();
            return;
        }

        List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
        int nextTotalPage = EnchantmentTableRules.calculatePageCount(enchantments.size(), generatedSlotCount, false);
        boolean needsInitialFill = totalPage == 0 || !hasVisibleGeneratedBook();
        if (needsInitialFill) {
            currentPage = 0;
            totalPage = nextTotalPage;
            clearGeneratedSlots();
            generateGeneratedSlots();
            return;
        }

        totalPage = nextTotalPage;
        if (currentPage >= totalPage) {
            currentPage = Math.max(0, totalPage - 1);
            clearGeneratedSlots();
        }
        generateGeneratedSlots();
    }

    public boolean canPickGeneratedBook() {
        return !copyMode.getAsBoolean()
                && inventory.getStackInSlot(bookSlot).is(Items.BOOK)
                && hasEnoughPayment();
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
                hasEnoughMaterialsForCopy(inventory, config.snapshot(), bookSlot, paymentSlot)
        )) {
            return TableOperationResult.failed(false);
        }

        inventory.getStackInSlot(bookSlot).shrink(1);
        EnchantmentTableRules.consumePayment(inventory.getStackInSlot(paymentSlot), config.snapshot());
        inventory.setStackInSlot(
                copyResultSlot,
                inventory.getStackInSlot(templateSlot).copyWithCount(1)
        );
        return TableOperationResult.success(true);
    }

    private ItemStack createEnchantedBook(Holder<Enchantment> enchantment) {
        int enchantmentLevel = config.snapshot().convertOnlyLevelOneBook() ? 1 : enchantment.value().getMaxLevel();
        return enchantments.createEnchantedBook(enchantment, enchantmentLevel);
    }

    private List<Holder<Enchantment>> getFilteredEnchantments() {
        List<Holder<Enchantment>> availableEnchantments = enchantments.allEnchantments(world);
        if (EnchantmentSearchRules.isBlankSearch(searchQuery)) {
            return availableEnchantments;
        }

        return availableEnchantments.stream()
                .filter(this::matchesSearch)
                .toList();
    }

    private boolean matchesSearch(Holder<Enchantment> enchantment) {
        var enchantmentId = enchantments.getCoreEnchantmentKey(world, enchantment);
        if (usingClientSearchMatches && clientMatchedEnchantments.contains(enchantmentId.asString())) {
            return true;
        }

        List<String> serverCandidates = new ArrayList<>();
        serverCandidates.add(enchantmentId.asString());
        serverCandidates.add(enchantmentId.namespace());
        serverCandidates.add(enchantmentId.path());
        serverCandidates.add(enchantmentId.path().replace('_', ' '));
        serverCandidates.add("enchantment." + enchantmentId.namespace() + "." + enchantmentId.path());
        serverCandidates.add(enchantment.value().description().getString());
        return EnchantmentSearchRules.matchesAnyCandidate(searchQuery, serverCandidates);
    }

    private boolean hasEnoughPayment() {
        return EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config.snapshot());
    }

    private boolean hasVisibleGeneratedBook() {
        for (int i = 0; i < generatedSlotCount; i++) {
            if (!inventory.getStackInSlot(generatedSlotStart + i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEnoughMaterialsForCopy(
            LogicalInventory inventory,
            TableConfigView config,
            int bookSlot,
            int paymentSlot
    ) {
        return inventory.getStackInSlot(bookSlot).is(Items.BOOK)
                && EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config);
    }
}
