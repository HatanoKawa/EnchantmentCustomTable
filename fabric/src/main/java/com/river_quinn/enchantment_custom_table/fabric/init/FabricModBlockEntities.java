package com.river_quinn.enchantment_custom_table.fabric.init;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FabricModBlockEntities {
    public static final BlockEntityType<FabricEnchantingCustomTableBlockEntity> ENCHANTING_CUSTOM_TABLE =
            BlockEntityType.Builder.of(FabricEnchantingCustomTableBlockEntity::new, FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK).build(null);

    public static final BlockEntityType<FabricEnchantmentConversionTableBlockEntity> ENCHANTMENT_CONVERSION_TABLE =
            BlockEntityType.Builder.of(FabricEnchantmentConversionTableBlockEntity::new, FabricModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK).build(null);

    private FabricModBlockEntities() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, EnchantmentCustomTableFabric.id("enchanting_custom_table"), ENCHANTING_CUSTOM_TABLE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, EnchantmentCustomTableFabric.id("enchantment_conversion_table"), ENCHANTMENT_CONVERSION_TABLE);
    }
}
