package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class FabricModItems {
    public static final BlockItem ENCHANTING_CUSTOM_TABLE_ITEM = new BlockItem(FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK, new Item.Properties());
    public static final BlockItem ENCHANTMENT_CONVERSION_TABLE_ITEM = new BlockItem(FabricModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK, new Item.Properties());

    private FabricModItems() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, EnchantmentCustomTableFabric.id("enchanting_custom_table"), ENCHANTING_CUSTOM_TABLE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, EnchantmentCustomTableFabric.id("enchantment_conversion_table"), ENCHANTMENT_CONVERSION_TABLE_ITEM);
    }
}
