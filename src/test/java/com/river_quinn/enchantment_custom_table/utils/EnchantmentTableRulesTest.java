package com.river_quinn.enchantment_custom_table.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentTableRulesTest {
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
}
