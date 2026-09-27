package com.river_quinn.enchantment_custom_table.core.config;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

/** Loader-independent payment validation. Registry resolution happens after item registration. */
public final class PaymentOptions {
    public static final int SLOT_CAPACITY = 64;
    public static final int MAX_ENTRIES = 256;
    private static final Pattern ITEM_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    public static final Map<String, Integer> DEFAULTS;

    static {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("minecraft:emerald", 36);
        values.put("minecraft:emerald_block", 4);
        values.put("minecraft:nether_star", 1);
        DEFAULTS = Collections.unmodifiableMap(values);
    }

    private PaymentOptions() {}

    public static boolean validId(String id) {
        return id != null && id.length() <= 256 && ITEM_ID.matcher(id).matches()
                && !id.equals("minecraft:air") && !id.equals("minecraft:book")
                && !id.equals("minecraft:enchanted_book");
    }

    public static int clamp(BigInteger cost, int maxStackSize) {
        return cost.max(BigInteger.ONE)
                .min(BigInteger.valueOf(Math.max(1, Math.min(SLOT_CAPACITY, maxStackSize)))).intValue();
    }

    public static Map<String, Integer> legacy(int emerald, int block) {
        Map<String, Integer> values = new LinkedHashMap<>();
        if (emerald > 0) values.put("minecraft:emerald", Math.min(SLOT_CAPACITY, emerald));
        if (block > 0) values.put("minecraft:emerald_block", Math.min(SLOT_CAPACITY, block));
        return values.isEmpty() ? DEFAULTS : values;
    }

    public static Map<String, Integer> resolve(Map<String, Integer> values,
            ToIntFunction<String> stackLimit, Consumer<String> warning) {
        Map<String, Integer> result = resolveEntries(values, stackLimit, warning);
        if (result.isEmpty()) {
            warning.accept("No usable payment entries; using the three default payment options");
            result = resolveEntries(DEFAULTS, stackLimit, warning);
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Integer> resolveEntries(Map<String, Integer> values,
            ToIntFunction<String> stackLimit, Consumer<String> warning) {
        Map<String, Integer> result = new LinkedHashMap<>();
        values.forEach((id, cost) -> {
            int limit = validId(id) ? stackLimit.applyAsInt(id) : 0;
            if (limit <= 0) {
                warning.accept("Ignoring unknown or disallowed payment item: " + id);
            } else {
                int effective = clamp(BigInteger.valueOf(cost), limit);
                if (effective != cost) warning.accept(id + ": cost " + cost + " corrected to " + effective);
                result.put(id, effective);
            }
        });
        return result;
    }
}
