package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricModBlockEntities {
    public static final BlockEntityType<FabricEnchantingCustomTableBlockEntity> ENCHANTING_CUSTOM_TABLE =
            FabricBlockEntityTypeBuilder.create(FabricEnchantingCustomTableBlockEntity::new, FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK).build();

    public static final BlockEntityType<FabricEnchantmentConversionTableBlockEntity> ENCHANTMENT_CONVERSION_TABLE =
            FabricBlockEntityTypeBuilder.create(FabricEnchantmentConversionTableBlockEntity::new, FabricModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK).build();

    private FabricModBlockEntities() {
    }

    public static void register() {
        FabricVersionedMinecraft.register(Registry.BLOCK_ENTITY_TYPE, "enchanting_custom_table", ENCHANTING_CUSTOM_TABLE);
        FabricVersionedMinecraft.register(Registry.BLOCK_ENTITY_TYPE, "enchantment_conversion_table", ENCHANTMENT_CONVERSION_TABLE);
    }
}
