package com.river_quinn.enchantment_custom_table.fabric.block.entity;

import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEnchantmentConversionTableBlockEntity extends FabricEnchantingTableLikeBlockEntity {
    public FabricEnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricModBlockEntities.ENCHANTMENT_CONVERSION_TABLE, pos, state);
    }
}
