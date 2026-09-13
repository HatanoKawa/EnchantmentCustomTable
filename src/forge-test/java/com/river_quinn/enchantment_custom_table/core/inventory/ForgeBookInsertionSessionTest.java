package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;

class ForgeBookInsertionSessionTest extends BookInsertionSessionTest {
    @Override
    protected EnchantmentAccessService enchantments() {
        return EnchantmentUtils.service();
    }
}
