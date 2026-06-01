package com.river_quinn.enchantment_custom_table.core.config;

public record TableConfigSnapshot(
        int minimumEmeraldCost,
        int minimumEmeraldBlockCost,
        boolean enforceEnchantmentLevelLimit,
        boolean incrementalSameLevelMerge,
        boolean convertOnlyLevelOneBook,
        boolean enableXpRequirement
) implements TableConfigView {
}
