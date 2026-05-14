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
import java.util.Set;
import java.util.function.BiPredicate;

public final class EnchantmentTableRules {
    private EnchantmentTableRules() {
    }

    public record EnchantmentLevel(Holder<Enchantment> enchantment, int level) {
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
        if (sourceLevel <= 1) {
            return List.of();
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
        for (EnchantmentLevel addition : additions) {
            int currentLevel = getMatchingEnchantmentLevel(currentEnchantments, addition.enchantment(), matcher);
            if (currentLevel + addition.level() > addition.enchantment().value().getMaxLevel()) {
                return false;
            }
        }
        return true;
    }

    public static ItemEnchantments mergeEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> additions,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(baseEnchantments);

        for (EnchantmentLevel addition : additions) {
            Optional<Holder<Enchantment>> existing = findMatchingEnchantment(mutable.toImmutable(), addition.enchantment(), matcher);
            if (existing.isPresent()) {
                mutable.set(existing.get(), mutable.getLevel(existing.get()) + addition.level());
            } else {
                mutable.set(addition.enchantment(), addition.level());
            }
        }

        return mutable.toImmutable();
    }

    public static ItemEnchantments subtractEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> removals,
            BiPredicate<Holder<Enchantment>, Holder<Enchantment>> matcher
    ) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(baseEnchantments);

        for (EnchantmentLevel removal : removals) {
            Optional<Holder<Enchantment>> existing = findMatchingEnchantment(mutable.toImmutable(), removal.enchantment(), matcher);
            existing.ifPresent(enchantment -> mutable.set(enchantment, mutable.getLevel(enchantment) - removal.level()));
        }

        return mutable.toImmutable();
    }
}
