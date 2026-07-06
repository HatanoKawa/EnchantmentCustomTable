package com.river_quinn.enchantment_custom_table.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableConfigSnapshotTest {
    @Test
    void snapshotExposesStableConfigValues() {
        TableConfigView config = new TableConfigSnapshot(36, 4, true, true, false, true);

        assertEquals(36, config.minimumEmeraldCost());
        assertEquals(4, config.minimumEmeraldBlockCost());
        assertTrue(config.enforceEnchantmentLevelLimit());
        assertTrue(config.incrementalSameLevelMerge());
        assertFalse(config.convertOnlyLevelOneBook());
        assertTrue(config.freeConversionTableCosts());
    }
}
