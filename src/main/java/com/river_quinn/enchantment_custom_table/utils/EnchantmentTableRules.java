package com.river_quinn.enchantment_custom_table.utils;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.rules.CopyRules;
import com.river_quinn.enchantment_custom_table.core.rules.MergeRules;
import com.river_quinn.enchantment_custom_table.core.rules.PaginationRules;
import com.river_quinn.enchantment_custom_table.core.rules.PaymentRules;
import com.river_quinn.enchantment_custom_table.core.rules.SplitRules;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;

public final class EnchantmentTableRules {
    private EnchantmentTableRules() {
    }

    public record EnchantmentLevel(Holder<Enchantment> enchantment, int level) {
    }

    public record MergeOptions(boolean enforceLevelLimit, boolean incrementalSameLevelMerge) {
        public static MergeOptions from(TableConfigView config) {
            return new MergeOptions(
                    config.enforceEnchantmentLevelLimit(),
                    config.incrementalSameLevelMerge()
            );
        }
    }

    public record MergeResult(boolean allowed, ItemEnchantments enchantments) {
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

    public static Optional<Holder<Enchantment>> findMatchingEnchantment(
            ItemEnchantments enchantments,
            Holder<Enchantment> target,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        for (var entry : enchantments.entrySet()) {
            if (matcher.test(entry.getKey(), target)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public static boolean containsMatchingEnchantment(
            List<EnchantmentLevel> enchantments,
            Holder<Enchantment> target,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        return enchantments.stream().anyMatch(enchantment -> matcher.test(enchantment.enchantment(), target));
    }

    public static int getMatchingEnchantmentLevel(
            ItemEnchantments enchantments,
            Holder<Enchantment> target,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        return findMatchingEnchantment(enchantments, target, matcher)
                .map(enchantments::getLevel)
                .orElse(0);
    }

    public static boolean canAddWithinMaxLevels(
            ItemEnchantments currentEnchantments,
            List<EnchantmentLevel> additions,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        return tryMergeEnchantments(
                currentEnchantments,
                additions,
                matcher,
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
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> additions,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher,
            MergeOptions options
    ) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(baseEnchantments);

        for (EnchantmentLevel addition : additions) {
            Optional<Holder<Enchantment>> existing = findMatchingEnchantment(mutable.toImmutable(), addition.enchantment(), matcher);
            int currentLevel = existing
                    .map(mutable::getLevel)
                    .orElse(0);
            OptionalInt resultLevel = calculateMergedEnchantmentLevel(
                    currentLevel,
                    addition.level(),
                    addition.enchantment().value().getMaxLevel(),
                    options
            );
            if (resultLevel.isEmpty()) {
                return new MergeResult(false, baseEnchantments);
            }

            mutable.set(existing.orElse(addition.enchantment()), resultLevel.getAsInt());
        }

        return new MergeResult(true, mutable.toImmutable());
    }

    public static ItemEnchantments mergeEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> additions,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        return tryMergeEnchantments(
                baseEnchantments,
                additions,
                matcher,
                new MergeOptions(false, false)
        ).enchantments();
    }

    public static ItemEnchantments subtractEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> removals,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        return subtractEnchantments(baseEnchantments, removals, matcher, false);
    }

    public static OptionalInt calculateRemainingEnchantmentLevel(
            int currentLevel,
            int removedLevel,
            boolean incrementalSingleBookSplit
    ) {
        return MergeRules.calculateRemainingEnchantmentLevel(currentLevel, removedLevel, incrementalSingleBookSplit);
    }

    public static ItemEnchantments subtractEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> removals,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher,
            boolean incrementalSingleBookSplit
    ) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(baseEnchantments);

        for (EnchantmentLevel removal : removals) {
            Optional<Holder<Enchantment>> existing = findMatchingEnchantment(mutable.toImmutable(), removal.enchantment(), matcher);
            existing.ifPresent(enchantment -> calculateRemainingEnchantmentLevel(
                    mutable.getLevel(enchantment),
                    removal.level(),
                    incrementalSingleBookSplit
            ).ifPresent(level -> mutable.set(enchantment, level)));
        }

        return mutable.toImmutable();
    }
}
