package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.fabric.block.FabricEnchantingCustomTableBlock;
import com.river_quinn.enchantment_custom_table.fabric.block.FabricEnchantmentConversionTableBlock;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public final class FabricModBlocks {
    public static final Block ENCHANTING_CUSTOM_TABLE_BLOCK = new FabricEnchantingCustomTableBlock();
    public static final Block ENCHANTMENT_CONVERSION_TABLE_BLOCK = new FabricEnchantmentConversionTableBlock();

    private FabricModBlocks() {
    }

    public static void register() {
        FabricVersionedMinecraft.register(Registry.BLOCK, "enchanting_custom_table", ENCHANTING_CUSTOM_TABLE_BLOCK);
        FabricVersionedMinecraft.register(Registry.BLOCK, "enchantment_conversion_table", ENCHANTMENT_CONVERSION_TABLE_BLOCK);
    }
}
