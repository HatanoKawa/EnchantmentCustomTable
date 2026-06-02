package com.river_quinn.enchantment_custom_table.core.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkIntentTest {
    @Test
    void enchantingIntentDecodesStableIdsLegacyNamesAndUnknownValues() {
        assertEquals(EnchantingTableIntent.NEXT_PAGE, EnchantingTableIntent.fromNetworkId("next_page"));
        assertEquals(EnchantingTableIntent.NEXT_PAGE, EnchantingTableIntent.fromNetworkId("NEXT_PAGE"));
        assertEquals(EnchantingTableIntent.UNKNOWN, EnchantingTableIntent.fromNetworkId("nope"));
        assertEquals(EnchantingTableIntent.PREVIOUS_PAGE, EnchantingTableIntent.fromLegacyName("PREVIOUS_PAGE"));
    }

    @Test
    void conversionIntentDecodesStableIdsLegacyNamesAndUnknownValues() {
        assertEquals(ConversionTableIntent.SEARCH, ConversionTableIntent.fromNetworkId("search"));
        assertEquals(ConversionTableIntent.SEARCH, ConversionTableIntent.fromNetworkId("SEARCH"));
        assertEquals(ConversionTableIntent.UNKNOWN, ConversionTableIntent.fromNetworkId("nope"));
        assertEquals(ConversionTableIntent.PREVIOUS_PAGE, ConversionTableIntent.fromLegacyName("PREVIOUS_PAGE"));
    }
}
