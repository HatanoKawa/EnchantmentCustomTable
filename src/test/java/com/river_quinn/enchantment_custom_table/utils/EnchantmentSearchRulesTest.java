package com.river_quinn.enchantment_custom_table.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantmentSearchRulesTest {
    @Test
    void normalizeSearchTextMakesRegistryPathsHumanSearchable() {
        assertEquals("soul speed", EnchantmentSearchRules.normalizeSearchText(" Soul_Speed "));
        assertEquals("quick charge", EnchantmentSearchRules.normalizeSearchText("quick-charge"));
        assertEquals("", EnchantmentSearchRules.normalizeSearchText(null));
    }

    @Test
    void matchesAnyCandidateSupportsNamespacedIdsAndLocalizedNames() {
        assertTrue(EnchantmentSearchRules.matchesAnyCandidate("sharp", List.of("minecraft:sharpness", "Sharpness")));
        assertTrue(EnchantmentSearchRules.matchesAnyCandidate("soul speed", List.of("minecraft:soul_speed")));
        assertTrue(EnchantmentSearchRules.matchesAnyCandidate("soulspeed", List.of("Soul Speed")));
        assertFalse(EnchantmentSearchRules.matchesAnyCandidate("frost", List.of("minecraft:sharpness", "Sharpness")));
    }

    @Test
    void sanitizeSearchQueryCapsNetworkPayloadText() {
        String oversizedQuery = "x".repeat(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH + 1);
        assertEquals(
                EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH,
                EnchantmentSearchRules.sanitizeSearchQuery(oversizedQuery).length()
        );
    }
}
