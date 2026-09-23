package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.ItemHandlerLogicalInventory;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.IntConsumer;

class ForgeConversionPaymentSessionTest extends ConversionPaymentSessionTest {
    @Override
    protected LogicalInventory inventory(IntConsumer changed) {
        return new ItemHandlerLogicalInventory(new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                changed.accept(slot);
            }
        });
    }

    @Override
    protected EnchantmentAccessService enchantments() {
        return EnchantmentUtils.service();
    }
}
