package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.JsonObject;

public final class JsonTableConfigCodec {
    public static final int DEFAULT_MINIMUM_EMERALD_COST = 36;
    public static final int DEFAULT_MINIMUM_EMERALD_BLOCK_COST = 4;
    public static final boolean DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT = false;
    public static final boolean DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE = false;
    public static final boolean DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK = false;
    public static final boolean DEFAULT_FREE_CONVERSION_TABLE_COSTS = false;

    public static final String MINIMUM_EMERALD_COST = "minimumEmeraldCost";
    public static final String MINIMUM_EMERALD_BLOCK_COST = "minimumEmeraldBlockCost";
    public static final String ENFORCE_ENCHANTMENT_LEVEL_LIMIT = "enforceEnchantmentLevelLimit";
    public static final String INCREMENTAL_SAME_LEVEL_MERGE = "incrementalSameLevelMerge";
    public static final String CONVERT_ONLY_LEVEL_ONE_BOOK = "convertOnlyLevelOneBook";
    public static final String FREE_CONVERSION_TABLE_COSTS = "freeConversionTableCosts";

    private JsonTableConfigCodec() {
    }

    public static TableConfigSnapshot defaultSnapshot() {
        return new TableConfigSnapshot(
                DEFAULT_MINIMUM_EMERALD_COST,
                DEFAULT_MINIMUM_EMERALD_BLOCK_COST,
                DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT,
                DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE,
                DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK,
                DEFAULT_FREE_CONVERSION_TABLE_COSTS
        );
    }

    public static JsonObject defaultJson() {
        JsonObject root = new JsonObject();
        root.addProperty(MINIMUM_EMERALD_COST, DEFAULT_MINIMUM_EMERALD_COST);
        root.addProperty(MINIMUM_EMERALD_BLOCK_COST, DEFAULT_MINIMUM_EMERALD_BLOCK_COST);
        root.addProperty(ENFORCE_ENCHANTMENT_LEVEL_LIMIT, DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT);
        root.addProperty(INCREMENTAL_SAME_LEVEL_MERGE, DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE);
        root.addProperty(CONVERT_ONLY_LEVEL_ONE_BOOK, DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK);
        root.addProperty(FREE_CONVERSION_TABLE_COSTS, DEFAULT_FREE_CONVERSION_TABLE_COSTS);
        return root;
    }

    public static TableConfigSnapshot parse(JsonObject root) {
        if (root == null) {
            return defaultSnapshot();
        }

        return new TableConfigSnapshot(
                intValue(root, MINIMUM_EMERALD_COST, DEFAULT_MINIMUM_EMERALD_COST),
                intValue(root, MINIMUM_EMERALD_BLOCK_COST, DEFAULT_MINIMUM_EMERALD_BLOCK_COST),
                booleanValue(root, ENFORCE_ENCHANTMENT_LEVEL_LIMIT, DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT),
                booleanValue(root, INCREMENTAL_SAME_LEVEL_MERGE, DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE),
                booleanValue(root, CONVERT_ONLY_LEVEL_ONE_BOOK, DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK),
                booleanValue(root, FREE_CONVERSION_TABLE_COSTS, DEFAULT_FREE_CONVERSION_TABLE_COSTS)
        );
    }

    private static int intValue(JsonObject root, String name, int defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsInt() : defaultValue;
    }

    private static boolean booleanValue(JsonObject root, String name, boolean defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsBoolean() : defaultValue;
    }
}
