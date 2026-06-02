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
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

public final class EnchantmentTableRules {
    private EnchantmentTableRules() {
    }

    public record EnchantmentLevel(Holder<Enchantment> enchantment, EnchantmentKey key, int level, int maxLevel) {
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
            EnchantmentKey target,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        for (var entry : enchantments.entrySet()) {
            if (keyResolver.apply(entry.getKey()).equals(target)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    public static boolean containsMatchingEnchantment(
            List<EnchantmentLevel> enchantments,
            EnchantmentKey target
    ) {
        return enchantments.stream().anyMatch(enchantment -> enchantment.key().equals(target));
    }

    public static int getMatchingEnchantmentLevel(
            ItemEnchantments enchantments,
            EnchantmentKey target,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        return findMatchingEnchantment(enchantments, target, keyResolver)
                .map(enchantments::getLevel)
                .orElse(0);
    }

    public static boolean canAddWithinMaxLevels(
            ItemEnchantments currentEnchantments,
            List<EnchantmentLevel> additions,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        return tryMergeEnchantments(
                currentEnchantments,
                additions,
                keyResolver,
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
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver,
            MergeOptions options
    ) {
        EnchantmentMergeRules.MergeResult result = EnchantmentMergeRules.tryMerge(
                toCoreList(baseEnchantments, keyResolver),
                additions.stream().map(EnchantmentLevel::toEntry).toList(),
                toCoreOptions(options)
        );
        if (!result.allowed()) {
            return new MergeResult(false, baseEnchantments);
        }

        return new MergeResult(
                true,
                toItemEnchantments(baseEnchantments, result.enchantments(), additions, keyResolver)
        );
    }

    public static ItemEnchantments mergeEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> additions,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        return tryMergeEnchantments(
                baseEnchantments,
                additions,
                keyResolver,
                new MergeOptions(false, false)
        ).enchantments();
    }

    public static ItemEnchantments subtractEnchantments(
            ItemEnchantments baseEnchantments,
            List<EnchantmentLevel> removals,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        return subtractEnchantments(baseEnchantments, removals, keyResolver, false);
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
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver,
            boolean incrementalSingleBookSplit
    ) {
        EnchantmentList result = EnchantmentMergeRules.subtract(
                toCoreList(baseEnchantments, keyResolver),
                removals.stream().map(EnchantmentLevel::toEntry).toList(),
                incrementalSingleBookSplit
        );
        return toItemEnchantments(baseEnchantments, result, removals, keyResolver);
    }

    private static EnchantmentMergeRules.MergeOptions toCoreOptions(MergeOptions options) {
        return new EnchantmentMergeRules.MergeOptions(
                options.enforceLevelLimit(),
                options.incrementalSameLevelMerge()
        );
    }

    private static EnchantmentList toCoreList(
            ItemEnchantments enchantments,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        return EnchantmentList.of(enchantments.entrySet().stream()
                .map(entry -> new EnchantmentEntry(
                        keyResolver.apply(entry.getKey()),
                        entry.getIntValue(),
                        entry.getKey().value().getMaxLevel()
                ))
                .toList());
    }

    private static ItemEnchantments toItemEnchantments(
            ItemEnchantments baseEnchantments,
            EnchantmentList result,
            List<EnchantmentLevel> changes,
            Function<Holder<Enchantment>, EnchantmentKey> keyResolver
    ) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(baseEnchantments);
        for (var entry : baseEnchantments.entrySet()) {
            int resultLevel = result.levelOf(keyResolver.apply(entry.getKey()));
            mutable.set(entry.getKey(), resultLevel);
        }

        for (EnchantmentEntry entry : result.entries()) {
            Optional<Holder<Enchantment>> existing = findMatchingEnchantment(mutable.toImmutable(), entry.key(), keyResolver);
            Holder<Enchantment> holder = existing
                    .or(() -> findChangeHolder(changes, entry.key()))
                    .orElse(null);
            if (holder != null) {
                mutable.set(holder, entry.level());
            }
        }
        return mutable.toImmutable();
    }

    private static Optional<Holder<Enchantment>> findChangeHolder(List<EnchantmentLevel> changes, EnchantmentKey key) {
        return changes.stream()
                .filter(change -> change.key().equals(key))
                .map(EnchantmentLevel::enchantment)
                .findFirst();
    }
}
