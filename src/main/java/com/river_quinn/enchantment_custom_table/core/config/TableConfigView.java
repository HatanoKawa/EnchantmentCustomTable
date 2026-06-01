package com.river_quinn.enchantment_custom_table.core.config;

public interface TableConfigView {
    int minimumEmeraldCost();

    int minimumEmeraldBlockCost();

    boolean enforceEnchantmentLevelLimit();

    boolean incrementalSameLevelMerge();

    boolean convertOnlyLevelOneBook();

    boolean enableXpRequirement();
}
