package com.river_quinn.enchantment_custom_table.core.config;

public record TableConfigSnapshot(
        java.util.Map<String, Integer> paymentOptions,
        boolean enforceEnchantmentLevelLimit,
        boolean incrementalSameLevelMerge,
        boolean convertOnlyLevelOneBook,
        boolean freeConversionTableCosts
) implements TableConfigView {
    public TableConfigSnapshot {
        paymentOptions = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(paymentOptions));
    }

    /** Migration/test convenience for the previous two-cost representation. */
    public TableConfigSnapshot(int emerald, int block, boolean enforce, boolean incremental, boolean levelOne, boolean free) {
        this(PaymentOptions.legacy(emerald, block), enforce, incremental, levelOne, free);
    }

    public TableConfigSnapshot withPayments(java.util.Map<String, Integer> payments) {
        return new TableConfigSnapshot(payments, enforceEnchantmentLevelLimit, incrementalSameLevelMerge,
                convertOnlyLevelOneBook, freeConversionTableCosts);
    }
}
