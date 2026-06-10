package com.river_quinn.enchantment_custom_table.fabric.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

abstract class FabricTableDropBlock extends BaseEntityBlock {
    protected FabricTableDropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            dropTableContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected abstract void dropTableContents(Level level, BlockPos pos);
}
