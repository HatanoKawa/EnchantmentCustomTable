package com.river_quinn.enchantment_custom_table.utils;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiPredicate;

public final class EnchantmentTableRules {
    private EnchantmentTableRules() {
    }

    public record EnchantmentLevel(Holder<Enchantment> enchantment, int level) {
    }

    public record MergeOptions(boolean enforceLevelLimit, boolean incrementalSameLevelMerge) {
    }

    public record MergeResult(boolean allowed, ItemEnchantments enchantments) {
    }

    public enum PaymentKind {
        EMERALD,
        EMERALD_BLOCK,
        UNSUPPORTED
    }

    public static int calculatePageCount(int entryCount, int pageSize, boolean keepEmptyPage) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }

        if (entryCount <= 0) {
            return keepEmptyPage ? 1 : 0;
        }

        return (entryCount + pageSize - 1) / pageSize;
    }

    public static int cacheIndexForGeneratedSlot(int slotIndex, int firstGeneratedSlotIndex, int currentPage, int pageSize) {
        return (slotIndex - firstGeneratedSlotIndex) + currentPage * pageSize;
    }

    public static boolean hasEnoughPayment(int availableCount, int configuredCost) {
        return configuredCost > 0 && availableCount >= configuredCost;
    }

    public static int remainingPaymentCount(int availableCount, int configuredCost) {
        if (!hasEnoughPayment(availableCount, configuredCost)) {
            return availableCount;
        }
        return availableCount - configuredCost;
    }

    public static int paymentCostForKind(PaymentKind paymentKind, int emeraldCost, int emeraldBlockCost) {
        if (paymentKind == PaymentKind.EMERALD && emeraldCost > 0) {
            return emeraldCost;
        }
        if (paymentKind == PaymentKind.EMERALD_BLOCK && emeraldBlockCost > 0) {
            return emeraldBlockCost;
        }
        return 0;
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

    public static boolean hasEnoughPayment(ItemStack paymentStack, int emeraldCost, int emeraldBlockCost) {
        return hasEnoughPayment(paymentStack.getCount(), paymentCostFor(paymentStack.getItem(), emeraldCost, emeraldBlockCost));
    }

    public static boolean consumePayment(ItemStack paymentStack, int emeraldCost, int emeraldBlockCost) {
        int cost = paymentCostFor(paymentStack.getItem(), emeraldCost, emeraldBlockCost);
        if (!hasEnoughPayment(paymentStack.getCount(), cost)) {
            return false;
        }
        paymentStack.setCount(remainingPaymentCount(paymentStack.getCount(), cost));
        return true;
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel) {
        return splitSingleEnchantmentLevels(sourceLevel, false);
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel, MergeOptions options) {
        return splitSingleEnchantmentLevels(sourceLevel, options.incrementalSameLevelMerge());
    }

    public static List<Integer> splitSingleEnchantmentLevels(int sourceLevel, boolean incrementalSameLevelMerge) {
        if (sourceLevel <= 1) {
            return List.of();
        }

        if (incrementalSameLevelMerge) {
            return List.of(sourceLevel - 1, sourceLevel - 1);
        }

        List<Integer> levels = new ArrayList<>();
        Set<Integer> seenLevels = new LinkedHashSet<>();
        int remainingLevel = sourceLevel;

        while (remainingLevel > 1) {
            int levelToAdd = remainingLevel / 2;
            if (seenLevels.add(levelToAdd)) {
                levels.add(levelToAdd);
            }

            remainingLevel -= levelToAdd;
        }

        return List.copyOf(levels);
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
        if (addedLevel <= 0) {
            return OptionalInt.empty();
        }

        int resultLevel;
        if (currentLevel <= 0) {
            resultLevel = addedLevel;
        } else if (options.incrementalSameLevelMerge()) {
            if (currentLevel != addedLevel) {
                return OptionalInt.empty();
            }
            resultLevel = currentLevel + 1;
        } else {
            resultLevel = currentLevel + addedLevel;
        }

        if (options.enforceLevelLimit() && resultLevel > maxLevel) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(resultLevel);
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
        if (currentLevel <= 0 || removedLevel <= 0) {
            return OptionalInt.empty();
        }

        if (incrementalSingleBookSplit) {
            return OptionalInt.of(currentLevel - 1);
        }

        return OptionalInt.of(currentLevel - removedLevel);
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
