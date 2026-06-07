package com.river_quinn.enchantment_custom_table.fabric.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

abstract class FabricTableDropBlock extends BaseEntityBlock {
    protected FabricTableDropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        dropTableContents(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    protected abstract void dropTableContents(Level level, BlockPos pos);
}
