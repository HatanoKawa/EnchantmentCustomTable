package com.river_quinn.enchantment_custom_table.core.access;

public interface EnchantmentAccess<S> {
    EnchantmentList getEnchantments(S source);

    S withEnchantments(S source, EnchantmentList enchantments);
}
