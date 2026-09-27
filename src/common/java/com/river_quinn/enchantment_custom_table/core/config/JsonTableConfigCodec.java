package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.*;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class JsonTableConfigCodec {
    public static final int DEFAULT_MINIMUM_EMERALD_COST = 36;
    public static final int DEFAULT_MINIMUM_EMERALD_BLOCK_COST = 4;
    public static final String PAYMENT_OPTIONS = "paymentOptions";
    public static final String MINIMUM_EMERALD_COST = "minimumEmeraldCost";
    public static final String MINIMUM_EMERALD_BLOCK_COST = "minimumEmeraldBlockCost";
    public static final String ENFORCE_ENCHANTMENT_LEVEL_LIMIT = "enforceEnchantmentLevelLimit";
    public static final String INCREMENTAL_SAME_LEVEL_MERGE = "incrementalSameLevelMerge";
    public static final String CONVERT_ONLY_LEVEL_ONE_BOOK = "convertOnlyLevelOneBook";
    public static final String FREE_CONVERSION_TABLE_COSTS = "freeConversionTableCosts";
    public static final boolean DEFAULT_ENFORCE_ENCHANTMENT_LEVEL_LIMIT = false;
    public static final boolean DEFAULT_INCREMENTAL_SAME_LEVEL_MERGE = false;
    public static final boolean DEFAULT_CONVERT_ONLY_LEVEL_ONE_BOOK = false;
    public static final boolean DEFAULT_FREE_CONVERSION_TABLE_COSTS = false;

    private JsonTableConfigCodec() {}

    public static TableConfigSnapshot defaultSnapshot() {
        return new TableConfigSnapshot(PaymentOptions.DEFAULTS, false, false, false, false);
    }

    public static JsonObject defaultJson() { return toJson(defaultSnapshot()); }

    public static JsonObject toJson(TableConfigView value) {
        JsonObject root = new JsonObject();
        root.addProperty("configVersion", 2);
        JsonArray entries = new JsonArray();
        value.paymentOptions().forEach((id, cost) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("item_id", id);
            entry.addProperty("cost", cost);
            entries.add(entry);
        });
        root.add(PAYMENT_OPTIONS, entries);
        root.addProperty(ENFORCE_ENCHANTMENT_LEVEL_LIMIT, value.enforceEnchantmentLevelLimit());
        root.addProperty(INCREMENTAL_SAME_LEVEL_MERGE, value.incrementalSameLevelMerge());
        root.addProperty(CONVERT_ONLY_LEVEL_ONE_BOOK, value.convertOnlyLevelOneBook());
        root.addProperty(FREE_CONVERSION_TABLE_COSTS, value.freeConversionTableCosts());
        return root;
    }

    public static boolean needsMigration(JsonObject root) {
        return !root.has(PAYMENT_OPTIONS)
                && (root.has(MINIMUM_EMERALD_COST) || root.has(MINIMUM_EMERALD_BLOCK_COST));
    }

    public static TableConfigSnapshot parse(JsonObject root) { return parse(root, message -> {}); }

    public static TableConfigSnapshot parse(JsonObject root, Consumer<String> warning) {
        if (root == null) return defaultSnapshot();
        Map<String, Integer> payments;
        if (root.has(PAYMENT_OPTIONS)) {
            payments = readPayments(root.get(PAYMENT_OPTIONS), warning);
        } else if (needsMigration(root)) {
            payments = PaymentOptions.legacy(legacyInt(root, MINIMUM_EMERALD_COST, 36),
                    legacyInt(root, MINIMUM_EMERALD_BLOCK_COST, 4));
            warning.accept("Migrating legacy emerald costs; old zero entries are omitted; an empty list uses defaults");
        } else {
            payments = PaymentOptions.DEFAULTS;
        }
        return new TableConfigSnapshot(payments,
                booleanValue(root, ENFORCE_ENCHANTMENT_LEVEL_LIMIT, warning),
                booleanValue(root, INCREMENTAL_SAME_LEVEL_MERGE, warning),
                booleanValue(root, CONVERT_ONLY_LEVEL_ONE_BOOK, warning),
                booleanValue(root, FREE_CONVERSION_TABLE_COSTS, warning));
    }

    public static Map<String, Integer> readPayments(JsonElement input, Consumer<String> warning) {
        Map<String, Integer> values = new LinkedHashMap<>();
        if (input != null && input.isJsonArray()) {
            int index = 0;
            for (JsonElement element : input.getAsJsonArray()) {
                String location = PAYMENT_OPTIONS + "[" + index++ + "]";
                if (values.size() >= PaymentOptions.MAX_ENTRIES) {
                    warning.accept("Payment list exceeds " + PaymentOptions.MAX_ENTRIES + " entries; remaining entries ignored");
                    break;
                }
                if (!element.isJsonObject()) { warning.accept(location + ": expected an object; ignored"); continue; }
                JsonObject entry = element.getAsJsonObject();
                JsonElement idValue = entry.get("item_id"), costValue = entry.get("cost");
                if (idValue == null || !idValue.isJsonPrimitive() || !idValue.getAsJsonPrimitive().isString()
                        || !PaymentOptions.validId(idValue.getAsString())) {
                    warning.accept(location + ": invalid/disallowed item_id; ignored"); continue;
                }
                String id = idValue.getAsString();
                if (costValue == null || !costValue.isJsonPrimitive() || !costValue.getAsJsonPrimitive().isNumber()
                        || !costValue.getAsString().matches("-?[0-9]+")) {
                    warning.accept(location + " (" + id + "): cost must be an integer; ignored"); continue;
                }
                if (values.containsKey(id)) { warning.accept(location + ": duplicate " + id + "; first entry wins"); continue; }
                BigInteger cost = new BigInteger(costValue.getAsString());
                int clamped = PaymentOptions.clamp(cost, PaymentOptions.SLOT_CAPACITY);
                if (!cost.equals(BigInteger.valueOf(clamped))) warning.accept(location + ": cost " + cost + " corrected to " + clamped);
                values.put(id, clamped);
            }
        } else {
            warning.accept(PAYMENT_OPTIONS + ": expected an array");
        }
        if (values.isEmpty()) {
            warning.accept("No payment entries remain; using 36 emeralds / 4 emerald blocks / 1 nether star");
            return PaymentOptions.DEFAULTS;
        }
        return values;
    }

    private static int legacyInt(JsonObject root, String name, int fallback) {
        try { return root.has(name) ? root.get(name).getAsBigDecimal().max(java.math.BigDecimal.ZERO)
                .min(java.math.BigDecimal.valueOf(64)).intValueExact() : fallback; }
        catch (RuntimeException ex) { return fallback; }
    }

    private static boolean booleanValue(JsonObject root, String name, Consumer<String> warning) {
        if (!root.has(name)) return false;
        JsonElement value = root.get(name);
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) return value.getAsBoolean();
        warning.accept(name + ": expected boolean; using false");
        return false;
    }
}
