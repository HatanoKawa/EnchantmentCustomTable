package com.river_quinn.enchantment_custom_table.core.session;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
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
import java.util.function.Supplier;

public class ConversionTableSession {
    private final Level world;
    private final LogicalInventory inventory;
    private final BooleanSupplier copyMode;
    private final Supplier<TableConfigView> config;
    private final int bookSlot;
    private final int paymentSlot;
    private final int generatedSlotStart;
    private final int generatedSlotCount;
    private final List<Holder<Enchantment>> allEnchantments = new ArrayList<>();

    private String searchQuery = "";
    private String searchClientLanguage = "";
    private boolean usingClientSearchMatches = false;
    //? if >=1.21.11 {
    private Set<Identifier> clientMatchedEnchantments = Set.of();
    //?} else {
    /*private Set<ResourceLocation> clientMatchedEnchantments = Set.of();
    *///?}
    private int currentPage = 0;
    private int totalPage = 0;

    public ConversionTableSession(
            Level world,
            LogicalInventory inventory,
            BooleanSupplier copyMode,
            Supplier<TableConfigView> config,
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

    //? if >=1.21.11 {
    public void setSearchQuery(String query, String clientLanguage, List<Identifier> matchedEnchantments) {
    //?} else {
    /*public void setSearchQuery(String query, String clientLanguage, List<ResourceLocation> matchedEnchantments) {
    *///?}
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
        EnchantmentTableRules.consumePayment(inventory.getStackInSlot(paymentSlot), config.get());
        generateGeneratedSlots();
        return TableOperationResult.success(true);
    }

    public static TableOperationResult refreshCopyResult(
            LogicalInventory inventory,
            BooleanSupplier copyMode,
            Supplier<TableConfigView> config,
            int bookSlot,
            int paymentSlot,
            int templateSlot,
            int copyResultSlot
    ) {
        if (!EnchantmentTableRules.shouldGenerateCopyResult(
                copyMode.getAsBoolean(),
                inventory.getStackInSlot(copyResultSlot).isEmpty(),
                hasEnoughMaterialsForCopy(inventory, config.get(), bookSlot, paymentSlot)
        )) {
            return TableOperationResult.failed(false);
        }

        inventory.getStackInSlot(bookSlot).shrink(1);
        EnchantmentTableRules.consumePayment(inventory.getStackInSlot(paymentSlot), config.get());
        inventory.setStackInSlot(
                copyResultSlot,
                inventory.getStackInSlot(templateSlot).copyWithCount(1)
        );
        return TableOperationResult.success(true);
    }

    private void loadAllEnchantments() {
        if (allEnchantments.isEmpty()) {
            Registry<Enchantment> fullEnchantmentList = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            fullEnchantmentList.asHolderIdMap().forEach(allEnchantments::add);
        }
    }

    private ItemStack createEnchantedBook(Holder<Enchantment> enchantment) {
        int enchantmentLevel = config.get().convertOnlyLevelOneBook() ? 1 : enchantment.value().getMaxLevel();
        return EnchantmentUtils.createEnchantedBook(enchantment, enchantmentLevel);
    }

    private List<Holder<Enchantment>> getFilteredEnchantments() {
        loadAllEnchantments();
        if (EnchantmentSearchRules.isBlankSearch(searchQuery)) {
            return allEnchantments;
        }

        return allEnchantments.stream()
                .filter(this::matchesSearch)
                .toList();
    }

    private boolean matchesSearch(Holder<Enchantment> enchantment) {
        //? if >=1.21.11 {
        Optional<Identifier> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::identifier);
        //?} else {
        /*Optional<ResourceLocation> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::location);
        *///?}
        if (enchantmentId.isPresent() && usingClientSearchMatches && clientMatchedEnchantments.contains(enchantmentId.get())) {
            return true;
        }

        List<String> serverCandidates = new ArrayList<>();
        enchantmentId.ifPresent(id -> {
            serverCandidates.add(id.toString());
            serverCandidates.add(id.getNamespace());
            serverCandidates.add(id.getPath());
            serverCandidates.add(id.getPath().replace('_', ' '));
            serverCandidates.add("enchantment." + id.getNamespace() + "." + id.getPath());
        });
        serverCandidates.add(enchantment.value().description().getString());
        return EnchantmentSearchRules.matchesAnyCandidate(searchQuery, serverCandidates);
    }

    private boolean hasEnoughPayment() {
        return EnchantmentTableRules.hasEnoughPayment(inventory.getStackInSlot(paymentSlot), config.get());
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
