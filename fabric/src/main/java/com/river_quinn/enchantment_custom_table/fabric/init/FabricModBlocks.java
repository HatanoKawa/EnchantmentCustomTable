package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import com.river_quinn.enchantment_custom_table.fabric.block.FabricEnchantingCustomTableBlock;
import com.river_quinn.enchantment_custom_table.fabric.block.FabricEnchantmentConversionTableBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public final class FabricModBlocks {
    public static final Block ENCHANTING_CUSTOM_TABLE_BLOCK = new FabricEnchantingCustomTableBlock();
    public static final Block ENCHANTMENT_CONVERSION_TABLE_BLOCK = new FabricEnchantmentConversionTableBlock();

    private FabricModBlocks() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, EnchantmentCustomTableFabric.id("enchanting_custom_table"), ENCHANTING_CUSTOM_TABLE_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, EnchantmentCustomTableFabric.id("enchantment_conversion_table"), ENCHANTMENT_CONVERSION_TABLE_BLOCK);
    }
}
