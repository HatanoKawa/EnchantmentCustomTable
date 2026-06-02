package com.river_quinn.enchantment_custom_table.fabric.block.entity;

import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEnchantingCustomTableBlockEntity extends FabricEnchantingTableLikeBlockEntity {
    public FabricEnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricModBlockEntities.ENCHANTING_CUSTOM_TABLE, pos, state);
    }
}
