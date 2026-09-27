package com.river_quinn.enchantment_custom_table.core.config;

public interface TableConfigView {
    java.util.Map<String, Integer> paymentOptions();

    default int paymentCost(String itemId) {
        return paymentOptions().getOrDefault(itemId, 0);
    }

    /** Compatibility accessors for old callers; runtime payment uses the complete map. */
    default int minimumEmeraldCost() { return paymentCost("minecraft:emerald"); }
    default int minimumEmeraldBlockCost() { return paymentCost("minecraft:emerald_block"); }

    boolean enforceEnchantmentLevelLimit();

    boolean incrementalSameLevelMerge();

    boolean convertOnlyLevelOneBook();

    boolean freeConversionTableCosts();
}
