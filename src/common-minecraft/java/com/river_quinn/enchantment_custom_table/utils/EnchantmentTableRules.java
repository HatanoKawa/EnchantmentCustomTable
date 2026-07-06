package com.river_quinn.enchantment_custom_table.utils;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import com.river_quinn.enchantment_custom_table.core.rules.CopyRules;
import com.river_quinn.enchantment_custom_table.core.rules.EnchantmentMergeRules;
import com.river_quinn.enchantment_custom_table.core.rules.MergeRules;
import com.river_quinn.enchantment_custom_table.core.rules.PaginationRules;
import com.river_quinn.enchantment_custom_table.core.rules.PaymentRules;
import com.river_quinn.enchantment_custom_table.core.rules.SplitRules;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public final class EnchantmentTableRules {
    private EnchantmentTableRules() {
    }

    public record EnchantmentLevel(Enchantment enchantment, EnchantmentKey key, int level, int maxLevel) {
        public EnchantmentEntry toEntry() {
            return new EnchantmentEntry(key, level, maxLevel);
        }
    }

    public record MergeOptions(boolean enforceLevelLimit, boolean incrementalSameLevelMerge) {
        public static MergeOptions from(TableConfigView config) {
            return new MergeOptions(
                    config.enforceEnchantmentLevelLimit(),
                    config.incrementalSameLevelMerge()
            );
        }
    }

    public record MergeResult(boolean allowed, EnchantmentList enchantments) {
    }

    public enum PaymentKind {
        EMERALD(PaymentRules.PaymentKind.EMERALD),
        EMERALD_BLOCK(PaymentRules.PaymentKind.EMERALD_BLOCK),
        UNSUPPORTED(PaymentRules.PaymentKind.UNSUPPORTED);

        private final PaymentRules.PaymentKind coreKind;

        PaymentKind(PaymentRules.PaymentKind coreKind) {
            this.coreKind = coreKind;
        }
    }

    public static int calculatePageCount(int entryCount, int pageSize, boolean keepEmptyPage) {
        return PaginationRules.calculatePageCount(entryCount, pageSize, keepEmptyPage);
    }

    public static int cacheIndexForGeneratedSlot(int slotIndex, int firstGeneratedSlotIndex, int currentPage, int pageSize) {
        return PaginationRules.cacheIndexForGeneratedSlot(slotIndex, firstGeneratedSlotIndex, currentPage, pageSize);
    }

    public static boolean hasEnoughPayment(int availableCount, int configuredCost) {
        return PaymentRules.hasEnoughPayment(availableCount, configuredCost);
    }

    public static int remainingPaymentCount(int availableCount, int configuredCost) {
        return PaymentRules.remainingPaymentCount(availableCount, configuredCost);
    }

    public static int paymentCostForKind(PaymentKind paymentKind, int emeraldCost, int emeraldBlockCost) {
        return PaymentRules.paymentCostForKind(paymentKind.coreKind, emeraldCost, emeraldBlockCost);
    }

    public static PaymentKind paymentKindFor(Item item) {
        if (item == Items.EMERALD) {
            return PaymentKind.EMERALD;
        }
        if (item == Items.EMERALD_BLOCK) {
            return PaymentKind.EMERALD_BLOCK;
        }
        return PaymentKind.UNSUPPORTED;
    }

    public static int paymentCostFor(Item item, int emeraldCost, int emeraldBlockCost) {
        return paymentCostForKind(paymentKindFor(item), emeraldCost, emeraldBlockCost);
    }

    public static int paymentCostFor(Item item, TableConfigView config) {
        return paymentCostFor(item, config.minimumEmeraldCost(), config.minimumEmeraldBlockCost());
    }

    public static boolean hasEnoughPayment(ItemStack paymentStack, int emeraldCost, int emeraldBlockCost) {
        return hasEnoughPayment(paymentStack.getCount(), paymentCostFor(paymentStack.getItem(), emeraldCost, emeraldBlockCost));
    }

    public static boolean hasEnoughPayment(ItemStack paymentStack, TableConfigView config) {
        return hasEnoughPayment(paymentStack, config.minimumEmeraldCost(), config.minimumEmeraldBlockCost());
    }

    public static boolean consumePayment(ItemStack paymentStack, int emeraldCost, int emeraldBlockCost) {
        int cost = paymentCostFor(paymentStack.getItem(), emeraldCost, emeraldBlockCost);
        if (!hasEnoughPayment(paymentStack.getCount(), cost)) {
            return false;
        }
        paymentStack.setCount(remainingPaymentCount(paymentStack.getCount(), cost));
        return true;
    }

    public static boolean consumePayment(ItemStack paymentStack, TableConfigView config) {
        return consumePayment(paymentStack, config.minimumEmeraldCost(), config.minimumEmeraldBlockCost());
    }

    public static boolean acceptsConversionBookInput(TableConfigView config) {
        return !config.freeConversionTableCosts();
    }

    public static boolean isConversionPaymentItem(ItemStack stack, TableConfigView config) {
        return !stack.isEmpty()
                && !config.freeConversionTableCosts()
                && paymentCostFor(stack.getItem(), config) > 0;
    }

    public static boolean hasRequiredConversionMaterials(ItemStack bookStack, ItemStack paymentStack, TableConfigView config) {
        return config.freeConversionTableCosts()
                || (bookStack.is(Items.BOOK) && hasEnoughPayment(paymentStack, config));
    }

    public static void consumeRequiredConversionMaterials(ItemStack bookStack, ItemStack paymentStack, TableConfigView config) {
        if (config.freeConversionTableCosts()) {
            return;
        }

        bookStack.shrink(1);
        consumePayment(paymentStack, config);
    }

    public static boolean isValidSingleEnchantmentTemplate(int enchantmentCount, int level, int maxLevel) {
        return CopyRules.isValidSingleEnchantmentTemplate(enchantmentCount, level, maxLevel);
    }

    public static boolean shouldGenerateCopyResult(boolean copyMode, boolean resultSlotEmpty, boolean hasEnoughMaterials) {
        return CopyRules.shouldGenerateCopyResult(copyMode, resultSlotEmpty, hasEnoughMaterials);
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel) {
        return splitSingleEnchantmentLevels(sourceLevel, false);
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel, MergeOptions options) {
        return splitSingleEnchantmentLevels(sourceLevel, options.incrementalSameLevelMerge());
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel, boolean incrementalSameLevelMerge) {
        return SplitRules.splitSingleEnchantmentLevels(sourceLevel, incrementalSameLevelMerge);
    }

    public static Optional<EnchantmentEntry> findMatchingEnchantment(EnchantmentList enchantments, EnchantmentKey target) {
        return enchantments.find(target);
    }

    public static boolean containsMatchingEnchantment(
            List<EnchantmentLevel> enchantments,
            EnchantmentKey target
    ) {
        return enchantments.stream().anyMatch(enchantment -> enchantment.key().equals(target));
    }

    public static int getMatchingEnchantmentLevel(EnchantmentList enchantments, EnchantmentKey target) {
        return enchantments.levelOf(target);
    }

    public static boolean canAddWithinMaxLevels(
            EnchantmentList currentEnchantments,
            List<EnchantmentLevel> additions
    ) {
        return tryMergeEnchantments(
                currentEnchantments,
                additions,
                new MergeOptions(true, false)
        ).allowed();
    }

    public static OptionalInt calculateMergedEnchantmentLevel(
            int currentLevel,
            int addedLevel,
            int maxLevel,
            MergeOptions options
    ) {
        return MergeRules.calculateMergedEnchantmentLevel(
                currentLevel,
                addedLevel,
                maxLevel,
                options.enforceLevelLimit(),
                options.incrementalSameLevelMerge()
        );
    }

    public static MergeResult tryMergeEnchantments(
            EnchantmentList baseEnchantments,
            List<EnchantmentLevel> additions,
            MergeOptions options
    ) {
        EnchantmentMergeRules.MergeResult result = EnchantmentMergeRules.tryMerge(
                baseEnchantments,
                additions.stream().map(EnchantmentLevel::toEntry).toList(),
                toCoreOptions(options)
        );
        if (!result.allowed()) {
            return new MergeResult(false, baseEnchantments);
        }

        return new MergeResult(
                true,
                result.enchantments()
        );
    }

    public static EnchantmentList mergeEnchantments(
            EnchantmentList baseEnchantments,
            List<EnchantmentLevel> additions
    ) {
        return tryMergeEnchantments(
                baseEnchantments,
                additions,
                new MergeOptions(false, false)
        ).enchantments();
    }

    public static EnchantmentList subtractEnchantments(
            EnchantmentList baseEnchantments,
            List<EnchantmentLevel> removals
    ) {
        return subtractEnchantments(baseEnchantments, removals, false);
    }

    public static OptionalInt calculateRemainingEnchantmentLevel(
            int currentLevel,
            int removedLevel,
            boolean incrementalSingleBookSplit
    ) {
        return MergeRules.calculateRemainingEnchantmentLevel(currentLevel, removedLevel, incrementalSingleBookSplit);
    }

    public static EnchantmentList subtractEnchantments(
            EnchantmentList baseEnchantments,
            List<EnchantmentLevel> removals,
            boolean incrementalSingleBookSplit
    ) {
        return EnchantmentMergeRules.subtract(
                baseEnchantments,
                removals.stream().map(EnchantmentLevel::toEntry).toList(),
                incrementalSingleBookSplit
        );
    }

    private static EnchantmentMergeRules.MergeOptions toCoreOptions(MergeOptions options) {
        return new EnchantmentMergeRules.MergeOptions(
                options.enforceLevelLimit(),
                options.incrementalSameLevelMerge()
        );
    }

}
