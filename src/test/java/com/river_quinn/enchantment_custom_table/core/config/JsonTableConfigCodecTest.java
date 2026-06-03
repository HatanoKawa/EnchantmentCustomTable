package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTableConfigCodecTest {
    @Test
    void emptyConfigUsesDefaults() {
        JsonTableConfigCodec.ParseResult result = JsonTableConfigCodec.parse(new JsonObject());

        assertEquals(JsonTableConfigCodec.defaultSnapshot(), result.snapshot());
        assertTrue(result.deprecatedWarnings().isEmpty());
    }

    @Test
    void fullConfigReadsCurrentFields() {
        JsonObject root = new JsonObject();
        root.addProperty(JsonTableConfigCodec.MINIMUM_EMERALD_COST, 12);
        root.addProperty(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST, 2);
        root.addProperty(JsonTableConfigCodec.ENFORCE_ENCHANTMENT_LEVEL_LIMIT, true);
        root.addProperty(JsonTableConfigCodec.INCREMENTAL_SAME_LEVEL_MERGE, true);
        root.addProperty(JsonTableConfigCodec.CONVERT_ONLY_LEVEL_ONE_BOOK, true);

        TableConfigSnapshot snapshot = JsonTableConfigCodec.parse(root).snapshot();

        assertEquals(12, snapshot.minimumEmeraldCost());
        assertEquals(2, snapshot.minimumEmeraldBlockCost());
        assertTrue(snapshot.enforceEnchantmentLevelLimit());
        assertTrue(snapshot.incrementalSameLevelMerge());
        assertTrue(snapshot.convertOnlyLevelOneBook());
    }

    @Test
    void deprecatedFieldsAtDefaultDoNotWarn() {
        JsonObject root = JsonTableConfigCodec.defaultJson();

        JsonTableConfigCodec.ParseResult result = JsonTableConfigCodec.parse(root);

        assertTrue(result.deprecatedWarnings().isEmpty());
    }

    @Test
    void deprecatedFieldsWarnWhenNonDefault() {
        JsonObject root = new JsonObject();
        root.addProperty(JsonTableConfigCodec.DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT, false);
        root.addProperty(JsonTableConfigCodec.DEPRECATED_CONVERT_MAX_LEVEL_BOOK, false);

        JsonTableConfigCodec.ParseResult result = JsonTableConfigCodec.parse(root);

        assertEquals(2, result.deprecatedWarnings().size());
        assertEquals(JsonTableConfigCodec.DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT, result.deprecatedWarnings().get(0).optionName());
        assertEquals(JsonTableConfigCodec.ENFORCE_ENCHANTMENT_LEVEL_LIMIT, result.deprecatedWarnings().get(0).replacementName());
        assertEquals(JsonTableConfigCodec.DEPRECATED_CONVERT_MAX_LEVEL_BOOK, result.deprecatedWarnings().get(1).optionName());
        assertEquals(JsonTableConfigCodec.CONVERT_ONLY_LEVEL_ONE_BOOK, result.deprecatedWarnings().get(1).replacementName());
    }

    @Test
    void deprecatedFieldsAreIgnoredForBehavior() {
        JsonObject root = new JsonObject();
        root.addProperty(JsonTableConfigCodec.ENFORCE_ENCHANTMENT_LEVEL_LIMIT, false);
        root.addProperty(JsonTableConfigCodec.CONVERT_ONLY_LEVEL_ONE_BOOK, false);
        root.addProperty(JsonTableConfigCodec.DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT, false);
        root.addProperty(JsonTableConfigCodec.DEPRECATED_CONVERT_MAX_LEVEL_BOOK, false);

        TableConfigSnapshot snapshot = JsonTableConfigCodec.parse(root).snapshot();

        assertFalse(snapshot.enforceEnchantmentLevelLimit());
        assertFalse(snapshot.convertOnlyLevelOneBook());
    }
}
