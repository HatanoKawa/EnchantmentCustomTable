package com.river_quinn.enchantment_custom_table.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentTableRulesTest {
    private static final EnchantmentTableRules.MergeOptions DIRECT_IGNORE_LIMITS =
            new EnchantmentTableRules.MergeOptions(true, false);
    private static final EnchantmentTableRules.MergeOptions DIRECT_ENFORCE_LIMITS =
            new EnchantmentTableRules.MergeOptions(false, false);
    private static final EnchantmentTableRules.MergeOptions INCREMENTAL_IGNORE_LIMITS =
            new EnchantmentTableRules.MergeOptions(true, true);
    private static final EnchantmentTableRules.MergeOptions INCREMENTAL_ENFORCE_LIMITS =
            new EnchantmentTableRules.MergeOptions(false, true);

    @Test
    void calculatePageCountReturnsZeroForEmptyListsByDefault() {
        assertEquals(0, EnchantmentTableRules.calculatePageCount(0, 28, false));
    }

    @Test
    void calculatePageCountCanKeepOneEmptyPageForContainerState() {
        assertEquals(1, EnchantmentTableRules.calculatePageCount(0, 28, true));
    }

    @Test
    void calculatePageCountRoundsUpPartialPages() {
        assertEquals(1, EnchantmentTableRules.calculatePageCount(28, 28, false));
        assertEquals(2, EnchantmentTableRules.calculatePageCount(29, 28, false));
        assertEquals(3, EnchantmentTableRules.calculatePageCount(57, 28, false));
    }

    @Test
    void calculatePageCountRejectsInvalidPageSize() {
        assertThrows(IllegalArgumentException.class, () -> EnchantmentTableRules.calculatePageCount(1, 0, false));
    }

    @Test
    void hasEnoughPaymentRequiresPositiveConfiguredCost() {
        assertTrue(EnchantmentTableRules.hasEnoughPayment(3, 3));
        assertFalse(EnchantmentTableRules.hasEnoughPayment(2, 3));
        assertFalse(EnchantmentTableRules.hasEnoughPayment(3, 0));
    }

    @Test
    void paymentCostForOnlyAllowsConfiguredConversionCurrencies() {
        assertEquals(36, EnchantmentTableRules.paymentCostForKind(EnchantmentTableRules.PaymentKind.EMERALD, 36, 4));
        assertEquals(4, EnchantmentTableRules.paymentCostForKind(EnchantmentTableRules.PaymentKind.EMERALD_BLOCK, 36, 4));
        assertEquals(0, EnchantmentTableRules.paymentCostForKind(EnchantmentTableRules.PaymentKind.EMERALD, 0, 4));
        assertEquals(0, EnchantmentTableRules.paymentCostForKind(EnchantmentTableRules.PaymentKind.UNSUPPORTED, 36, 4));
    }

    @Test
    void remainingPaymentCountOnlyConsumesWhenTheConfiguredCostCanBePaid() {
        assertEquals(0, EnchantmentTableRules.remainingPaymentCount(36, 36));
        assertEquals(1, EnchantmentTableRules.remainingPaymentCount(37, 36));
        assertEquals(35, EnchantmentTableRules.remainingPaymentCount(35, 36));
        assertEquals(36, EnchantmentTableRules.remainingPaymentCount(36, 0));
    }

    @Test
    void cacheIndexForGeneratedSlotUsesTheCurrentPageOffset() {
        assertEquals(0, EnchantmentTableRules.cacheIndexForGeneratedSlot(2, 2, 0, 24));
        assertEquals(23, EnchantmentTableRules.cacheIndexForGeneratedSlot(25, 2, 0, 24));
        assertEquals(24, EnchantmentTableRules.cacheIndexForGeneratedSlot(2, 2, 1, 24));
        assertEquals(55, EnchantmentTableRules.cacheIndexForGeneratedSlot(29, 2, 1, 28));
    }

    @Test
    void splitSingleEnchantmentLevelsUsesFloorHalvesAndUniqueResults() {
        assertEquals(List.of(4, 2, 1), EnchantmentTableRules.splitSingleEnchantmentLevels(8));
        assertEquals(List.of(2, 1), EnchantmentTableRules.splitSingleEnchantmentLevels(5));
        assertEquals(List.of(1), EnchantmentTableRules.splitSingleEnchantmentLevels(3));
        assertEquals(List.of(), EnchantmentTableRules.splitSingleEnchantmentLevels(1));
    }

    @Test
    void splitSingleEnchantmentLevelsUsesMinusOnePairsInIncrementalMode() {
        assertEquals(List.of(7, 7), EnchantmentTableRules.splitSingleEnchantmentLevels(8, true));
        assertEquals(List.of(1, 1), EnchantmentTableRules.splitSingleEnchantmentLevels(2, true));
        assertEquals(List.of(), EnchantmentTableRules.splitSingleEnchantmentLevels(1, true));
    }

    @Test
    void directMergeAddsLevelsAndCanIgnoreVanillaCaps() {
        assertEquals(8, mergedLevel(4, 4, 5, DIRECT_IGNORE_LIMITS));
    }

    @Test
    void directMergeRejectsOverCapResultsWhenLimitsAreEnforced() {
        assertTrue(EnchantmentTableRules.calculateMergedEnchantmentLevel(
                4,
                4,
                5,
                DIRECT_ENFORCE_LIMITS
        ).isEmpty());
    }

    @Test
    void incrementalMergeAddsOneForMatchingLevels() {
        assertEquals(6, mergedLevel(5, 5, 5, INCREMENTAL_IGNORE_LIMITS));
    }

    @Test
    void incrementalMergeRejectsDifferentLevels() {
        assertTrue(EnchantmentTableRules.calculateMergedEnchantmentLevel(
                5,
                4,
                5,
                INCREMENTAL_IGNORE_LIMITS
        ).isEmpty());
    }

    @Test
    void incrementalMergeStillObeysVanillaCapsWhenLimitsAreEnforced() {
        assertTrue(EnchantmentTableRules.calculateMergedEnchantmentLevel(
                5,
                5,
                5,
                INCREMENTAL_ENFORCE_LIMITS
        ).isEmpty());
    }

    @Test
    void incrementalModeDoesNotRestrictNewEnchantments() {
        assertEquals(3, mergedLevel(0, 3, 5, INCREMENTAL_ENFORCE_LIMITS));
    }

    @Test
    void directRemovalSubtractsTheRemovedBookLevel() {
        assertEquals(1, remainingLevel(5, 4, false));
        assertEquals(0, remainingLevel(5, 5, false));
    }

    @Test
    void incrementalSingleBookSplitRemovalOnlyDropsOneLevel() {
        assertEquals(4, remainingLevel(5, 4, true));
        assertEquals(1, remainingLevel(2, 1, true));
    }

    private static int mergedLevel(
            int currentLevel,
            int addedLevel,
            int maxLevel,
            EnchantmentTableRules.MergeOptions options
    ) {
        return EnchantmentTableRules.calculateMergedEnchantmentLevel(
                currentLevel,
                addedLevel,
                maxLevel,
                options
        ).orElseThrow();
    }

    private static int remainingLevel(int currentLevel, int removedLevel, boolean incrementalSingleBookSplit) {
        return EnchantmentTableRules.calculateRemainingEnchantmentLevel(
                currentLevel,
                removedLevel,
                incrementalSingleBookSplit
        ).orElseThrow();
    }
}
