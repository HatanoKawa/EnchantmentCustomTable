package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;

class FabricBookInsertionSessionTest extends BookInsertionSessionTest {
    @Override
    protected EnchantmentAccessService enchantments() {
        return FabricEnchantmentUtils.service();
    }
}
