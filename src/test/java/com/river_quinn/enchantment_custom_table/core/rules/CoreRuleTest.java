package com.river_quinn.enchantment_custom_table.core.rules;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreRuleTest {
    private static final EnchantmentKey SHARPNESS = EnchantmentKey.of("minecraft", "sharpness");
    private static final EnchantmentKey UNBREAKING = EnchantmentKey.of("minecraft", "unbreaking");

    @Test
    void directMergeAddsDuplicateLevelsWhenLimitsAreDisabled() {
        OptionalInt result = MergeRules.calculateMergedEnchantmentLevel(5, 5, 5, false, false);

        assertEquals(OptionalInt.of(10), result);
    }

    @Test
    void levelLimitOnlyRejectsDuplicateMergeAboveMax() {
        OptionalInt newOvercapEntry = MergeRules.calculateMergedEnchantmentLevel(0, 7, 5, true, false);
        OptionalInt duplicateOvercapMerge = MergeRules.calculateMergedEnchantmentLevel(5, 5, 5, true, false);

        assertEquals(OptionalInt.of(7), newOvercapEntry);
        assertEquals(OptionalInt.empty(), duplicateOvercapMerge);
    }

    @Test
    void incrementalMergeRequiresSameLevelAndIncrementsByOne() {
        assertEquals(OptionalInt.of(6), MergeRules.calculateMergedEnchantmentLevel(5, 5, 10, false, true));
        assertEquals(OptionalInt.empty(), MergeRules.calculateMergedEnchantmentLevel(5, 4, 10, false, true));
    }

    @Test
    void splitRulesKeepOriginalBinaryAndIncrementalModes() {
        assertEquals(List.of(4, 2, 1), SplitRules.splitSingleEnchantmentLevels(8, false));
        assertEquals(List.of(2, 1), SplitRules.splitSingleEnchantmentLevels(5, false));
        assertEquals(List.of(1), SplitRules.splitSingleEnchantmentLevels(3, false));
        assertEquals(List.of(7, 7), SplitRules.splitSingleEnchantmentLevels(8, true));
        assertEquals(List.of(1, 1), SplitRules.splitSingleEnchantmentLevels(2, true));
        assertEquals(List.of(), SplitRules.splitSingleEnchantmentLevels(1, true));
    }

    @Test
    void paymentRulesConsumeOnlyWhenConfiguredCostCanBePaid() {
        assertTrue(PaymentRules.hasEnoughPayment(3, 3));
        assertFalse(PaymentRules.hasEnoughPayment(2, 3));
        assertFalse(PaymentRules.hasEnoughPayment(3, 0));
        assertEquals(28, PaymentRules.remainingPaymentCount(64, 36));
        assertEquals(1, PaymentRules.remainingPaymentCount(37, 36));
        assertEquals(3, PaymentRules.remainingPaymentCount(3, 36));
        assertEquals(0, PaymentRules.paymentCostForKind(PaymentRules.PaymentKind.EMERALD, 0, 4));
        assertEquals(36, PaymentRules.paymentCostForKind(PaymentRules.PaymentKind.EMERALD, 36, 4));
        assertEquals(4, PaymentRules.paymentCostForKind(PaymentRules.PaymentKind.EMERALD_BLOCK, 36, 4));
        assertEquals(0, PaymentRules.paymentCostForKind(PaymentRules.PaymentKind.UNSUPPORTED, 36, 4));
    }

    @Test
    void paginationRejectsInvalidPageSize() {
        assertEquals(0, PaginationRules.calculatePageCount(0, 24, false));
        assertEquals(1, PaginationRules.calculatePageCount(0, 24, true));
        assertEquals(1, PaginationRules.calculatePageCount(24, 24, false));
        assertEquals(2, PaginationRules.calculatePageCount(25, 24, false));
        assertEquals(3, PaginationRules.calculatePageCount(57, 28, false));
        assertEquals(26, PaginationRules.cacheIndexForGeneratedSlot(4, 2, 1, 24));
        assertEquals(55, PaginationRules.cacheIndexForGeneratedSlot(29, 2, 1, 28));
        assertThrows(IllegalArgumentException.class, () -> PaginationRules.calculatePageCount(1, 0, false));
    }

    @Test
    void copyRulesValidateSingleLegalTemplate() {
        assertTrue(CopyRules.isValidSingleEnchantmentTemplate(1, 5, 5));
        assertFalse(CopyRules.isValidSingleEnchantmentTemplate(2, 5, 5));
        assertFalse(CopyRules.isValidSingleEnchantmentTemplate(1, 6, 5));
        assertTrue(CopyRules.shouldGenerateCopyResult(true, true, true));
        assertFalse(CopyRules.shouldGenerateCopyResult(true, false, true));
    }

    @Test
    void removalRulesRespectDirectAndIncrementalSplitModes() {
        assertEquals(OptionalInt.of(1), MergeRules.calculateRemainingEnchantmentLevel(5, 4, false));
        assertEquals(OptionalInt.of(0), MergeRules.calculateRemainingEnchantmentLevel(5, 5, false));
        assertEquals(OptionalInt.of(4), MergeRules.calculateRemainingEnchantmentLevel(5, 4, true));
        assertEquals(OptionalInt.of(1), MergeRules.calculateRemainingEnchantmentLevel(2, 1, true));
        assertEquals(OptionalInt.empty(), MergeRules.calculateRemainingEnchantmentLevel(0, 1, true));
    }

    @Test
    void enchantmentMergeRulesUseStableKeysForDuplicateEntries() {
        EnchantmentList base = EnchantmentList.of(List.of(new EnchantmentEntry(SHARPNESS, 5, 10)));

        EnchantmentMergeRules.MergeResult result = EnchantmentMergeRules.tryMerge(
                base,
                List.of(new EnchantmentEntry(SHARPNESS, 5, 10)),
                new EnchantmentMergeRules.MergeOptions(false, true)
        );

        assertTrue(result.allowed());
        assertEquals(6, result.enchantments().levelOf(SHARPNESS));
    }

    @Test
    void enchantmentMergeRulesRejectInvalidMultiBookAtomically() {
        EnchantmentList base = EnchantmentList.of(List.of(new EnchantmentEntry(SHARPNESS, 5, 5)));

        EnchantmentMergeRules.MergeResult result = EnchantmentMergeRules.tryMerge(
                base,
                List.of(
                        new EnchantmentEntry(SHARPNESS, 5, 5),
                        new EnchantmentEntry(UNBREAKING, 3, 3)
                ),
                new EnchantmentMergeRules.MergeOptions(true, false)
        );

        assertFalse(result.allowed());
        assertEquals(5, result.enchantments().levelOf(SHARPNESS));
        assertEquals(0, result.enchantments().levelOf(UNBREAKING));
    }

    @Test
    void enchantmentMergeRulesAllowNewOvercapEntriesWhenLimitsAreEnforced() {
        EnchantmentList base = EnchantmentList.of(List.of(new EnchantmentEntry(SHARPNESS, 5, 5)));

        EnchantmentMergeRules.MergeResult result = EnchantmentMergeRules.tryMerge(
                base,
                List.of(new EnchantmentEntry(UNBREAKING, 7, 3)),
                new EnchantmentMergeRules.MergeOptions(true, false)
        );

        assertTrue(result.allowed());
        assertEquals(5, result.enchantments().levelOf(SHARPNESS));
        assertEquals(7, result.enchantments().levelOf(UNBREAKING));
    }

    @Test
    void enchantmentMergeRulesSubtractIncrementalSplitByOneLevel() {
        EnchantmentList base = EnchantmentList.of(List.of(new EnchantmentEntry(SHARPNESS, 5, 5)));

        EnchantmentList result = EnchantmentMergeRules.subtract(
                base,
                List.of(new EnchantmentEntry(SHARPNESS, 4, 5)),
                true
        );

        assertEquals(4, result.levelOf(SHARPNESS));
    }
}
