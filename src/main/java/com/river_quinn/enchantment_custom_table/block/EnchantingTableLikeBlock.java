package com.river_quinn.enchantment_custom_table.block;

import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
//? if >=1.21.5 {
import net.minecraft.server.level.ServerLevel;
//?}
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class EnchantingTableLikeBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);
    public EnchantingTableLikeBlock(Properties properties) {
        super(properties
                .lightLevel(blockState -> 15)
                .destroyTime(1)
                .explosionResistance(3600));
    }

    //? if <1.21.2 {
    /*public EnchantingTableLikeBlock() {
        super(Properties.of()
                .lightLevel(blockState -> 15)
                .destroyTime(1)
                .explosionResistance(3600));
    }
    *///?}

    //? if >=1.21.2 {
    //? if >=1.21.11 {
    public EnchantingTableLikeBlock(Identifier registryName) {
    //?} else {
    /*public EnchantingTableLikeBlock(ResourceLocation registryName) {
    *///?}
        super(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, registryName))
                .lightLevel(blockState -> 15)
                .destroyTime(1)
                .explosionResistance(3600));
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        return;
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return null;
    }

    //? if >=1.21.5 {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        dropTableContents(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
    //?} else {
    /*@Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            dropTableContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    *///?}

    private void dropTableContents(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingCustomTableBlockEntity table) {
            table.dropInventory();
            level.updateNeighbourForOutputSignal(pos, this);
        } else if (blockEntity instanceof EnchantmentConversionTableBlockEntity table) {
            table.dropInventory();
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        //? if >=1.21.2 {
        return level.isClientSide() ? (lvl, pos, blockState, t) -> {
        //?} else {
        /*return level.isClientSide ? (lvl, pos, blockState, t) -> {
        *///?}
            if (t instanceof EnchantingTableLikeBlockEntity enchantingTable) {
                EnchantingTableLikeBlockEntity.bookAnimationTick(lvl, pos, blockState, enchantingTable);
            }
        } : null;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

}
