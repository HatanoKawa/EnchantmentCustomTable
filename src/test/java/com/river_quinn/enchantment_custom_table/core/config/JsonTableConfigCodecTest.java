package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTableConfigCodecTest {
    @Test
    void emptyConfigUsesDefaults() {
        TableConfigSnapshot snapshot = JsonTableConfigCodec.parse(new JsonObject());

        assertEquals(JsonTableConfigCodec.defaultSnapshot(), snapshot);
    }

    @Test
    void nullConfigUsesDefaults() {
        TableConfigSnapshot snapshot = JsonTableConfigCodec.parse(null);

        assertEquals(JsonTableConfigCodec.defaultSnapshot(), snapshot);
    }

    @Test
    void fullConfigReadsCurrentFields() {
        JsonObject root = new JsonObject();
        root.addProperty(JsonTableConfigCodec.MINIMUM_EMERALD_COST, 12);
        root.addProperty(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST, 2);
        root.addProperty(JsonTableConfigCodec.ENFORCE_ENCHANTMENT_LEVEL_LIMIT, true);
        root.addProperty(JsonTableConfigCodec.INCREMENTAL_SAME_LEVEL_MERGE, true);
        root.addProperty(JsonTableConfigCodec.CONVERT_ONLY_LEVEL_ONE_BOOK, true);
        root.addProperty(JsonTableConfigCodec.FREE_CONVERSION_TABLE_COSTS, true);

        TableConfigSnapshot snapshot = JsonTableConfigCodec.parse(root);

        assertEquals(12, snapshot.minimumEmeraldCost());
        assertEquals(2, snapshot.minimumEmeraldBlockCost());
        assertTrue(snapshot.enforceEnchantmentLevelLimit());
        assertTrue(snapshot.incrementalSameLevelMerge());
        assertTrue(snapshot.convertOnlyLevelOneBook());
        assertTrue(snapshot.freeConversionTableCosts());
    }

    @Test
    void defaultJsonContainsOnlyCurrentFields() {
        JsonObject root = JsonTableConfigCodec.defaultJson();

        assertTrue(root.has(JsonTableConfigCodec.MINIMUM_EMERALD_COST));
        assertTrue(root.has(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST));
        assertTrue(root.has(JsonTableConfigCodec.ENFORCE_ENCHANTMENT_LEVEL_LIMIT));
        assertTrue(root.has(JsonTableConfigCodec.INCREMENTAL_SAME_LEVEL_MERGE));
        assertTrue(root.has(JsonTableConfigCodec.CONVERT_ONLY_LEVEL_ONE_BOOK));
        assertTrue(root.has(JsonTableConfigCodec.FREE_CONVERSION_TABLE_COSTS));
        assertFalse(root.has("ignoreEnchantmentLevelLimit"));
        assertFalse(root.has("convert_max_level_book"));
    }
}
