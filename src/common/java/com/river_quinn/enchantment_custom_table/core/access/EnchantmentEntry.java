package com.river_quinn.enchantment_custom_table.core.access;

import java.util.Objects;

public record EnchantmentEntry(EnchantmentKey key, int level, int maxLevel) {
    public EnchantmentEntry {
        Objects.requireNonNull(key, "key");
    }

    public EnchantmentEntry withLevel(int level) {
        return new EnchantmentEntry(key, level, maxLevel);
    }
}
