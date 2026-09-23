package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;

import java.util.function.IntConsumer;

class FabricConversionPaymentSessionTest extends ConversionPaymentSessionTest {
    @Override
    protected LogicalInventory inventory(IntConsumer changed) {
        return new FabricTableInventory(3, slot -> 64, (slot, stack) -> true, changed);
    }

    @Override
    protected EnchantmentAccessService enchantments() {
        return FabricEnchantmentUtils.service();
    }
}
