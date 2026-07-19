package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;

public final class FabricModItems {
    public static final BlockItem ENCHANTING_CUSTOM_TABLE_ITEM = new BlockItem(
            FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK,
            FabricVersionedMinecraft.itemProperties("enchanting_custom_table")
    );
    public static final BlockItem ENCHANTMENT_CONVERSION_TABLE_ITEM = new BlockItem(
            FabricModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK,
            FabricVersionedMinecraft.itemProperties("enchantment_conversion_table")
    );

    private FabricModItems() {
    }

    public static void register() {
        FabricVersionedMinecraft.register(Registry.ITEM, "enchanting_custom_table", ENCHANTING_CUSTOM_TABLE_ITEM);
        FabricVersionedMinecraft.register(Registry.ITEM, "enchantment_conversion_table", ENCHANTMENT_CONVERSION_TABLE_ITEM);
    }
}
