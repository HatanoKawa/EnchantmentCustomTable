package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JsonTableConfigCodec {
    public static final int DEFAULT_MINIMUM_EMERALD_COST = 36;
    public static final int DEFAULT_MINIMUM_EMERALD_BLOCK_COST = 4;
    public static final boolean DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT = false;
    public static final boolean DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE = false;
    public static final boolean DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK = false;
    public static final boolean DEFAULT_DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT = true;
    public static final boolean DEFAULT_DEPRECATED_CONVERT_MAX_LEVEL_BOOK = true;

    public static final String MINIMUM_EMERALD_COST = "minimumEmeraldCost";
    public static final String MINIMUM_EMERALD_BLOCK_COST = "minimumEmeraldBlockCost";
    public static final String ENFORCE_ENCHANTMENT_LEVEL_LIMIT = "enforceEnchantmentLevelLimit";
    public static final String INCREMENTAL_SAME_LEVEL_MERGE = "incrementalSameLevelMerge";
    public static final String CONVERT_ONLY_LEVEL_ONE_BOOK = "convertOnlyLevelOneBook";
    public static final String DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT = "ignoreEnchantmentLevelLimit";
    public static final String DEPRECATED_CONVERT_MAX_LEVEL_BOOK = "convert_max_level_book";

    private JsonTableConfigCodec() {
    }

    public static TableConfigSnapshot defaultSnapshot() {
        return new TableConfigSnapshot(
                DEFAULT_MINIMUM_EMERALD_COST,
                DEFAULT_MINIMUM_EMERALD_BLOCK_COST,
                DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT,
                DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE,
                DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK
        );
    }

    public static JsonObject defaultJson() {
        JsonObject root = new JsonObject();
        root.addProperty(MINIMUM_EMERALD_COST, DEFAULT_MINIMUM_EMERALD_COST);
        root.addProperty(MINIMUM_EMERALD_BLOCK_COST, DEFAULT_MINIMUM_EMERALD_BLOCK_COST);
        root.addProperty(ENFORCE_ENCHANTMENT_LEVEL_LIMIT, DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT);
        root.addProperty(INCREMENTAL_SAME_LEVEL_MERGE, DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE);
        root.addProperty(CONVERT_ONLY_LEVEL_ONE_BOOK, DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK);
        root.addProperty(DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT, DEFAULT_DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT);
        root.addProperty(DEPRECATED_CONVERT_MAX_LEVEL_BOOK, DEFAULT_DEPRECATED_CONVERT_MAX_LEVEL_BOOK);
        return root;
    }

    public static ParseResult parse(JsonObject root) {
        if (root == null) {
            return new ParseResult(defaultSnapshot(), List.of());
        }

        TableConfigSnapshot snapshot = new TableConfigSnapshot(
                intValue(root, MINIMUM_EMERALD_COST, DEFAULT_MINIMUM_EMERALD_COST),
                intValue(root, MINIMUM_EMERALD_BLOCK_COST, DEFAULT_MINIMUM_EMERALD_BLOCK_COST),
                booleanValue(root, ENFORCE_ENCHANTMENT_LEVEL_LIMIT, DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT),
                booleanValue(root, INCREMENTAL_SAME_LEVEL_MERGE, DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE),
                booleanValue(root, CONVERT_ONLY_LEVEL_ONE_BOOK, DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK)
        );

        List<DeprecatedOptionWarning> warnings = new ArrayList<>();
        addDeprecatedWarningIfChanged(
                warnings,
                root,
                DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT,
                DEFAULT_DEPRECATED_IGNORE_ENCHANTMENT_LEVEL_LIMIT,
                ENFORCE_ENCHANTMENT_LEVEL_LIMIT
        );
        addDeprecatedWarningIfChanged(
                warnings,
                root,
                DEPRECATED_CONVERT_MAX_LEVEL_BOOK,
                DEFAULT_DEPRECATED_CONVERT_MAX_LEVEL_BOOK,
                CONVERT_ONLY_LEVEL_ONE_BOOK
        );
        return new ParseResult(snapshot, List.copyOf(warnings));
    }

    private static int intValue(JsonObject root, String name, int defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsInt() : defaultValue;
    }

    private static boolean booleanValue(JsonObject root, String name, boolean defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsBoolean() : defaultValue;
    }

    private static void addDeprecatedWarningIfChanged(
            List<DeprecatedOptionWarning> warnings,
            JsonObject root,
            String name,
            boolean defaultValue,
            String replacementName
    ) {
        if (root.has(name) && root.get(name).isJsonPrimitive() && root.get(name).getAsBoolean() != defaultValue) {
            warnings.add(new DeprecatedOptionWarning(name, replacementName));
        }
    }

    public record ParseResult(TableConfigSnapshot snapshot, List<DeprecatedOptionWarning> deprecatedWarnings) {
    }

    public record DeprecatedOptionWarning(String optionName, String replacementName) {
    }
}
