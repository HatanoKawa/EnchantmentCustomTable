package com.river_quinn.enchantment_custom_table.fabric.block;

import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingTableLikeBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class FabricEnchantingTableLikeBlock extends FabricTableDropBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    protected FabricEnchantingTableLikeBlock(BlockBehaviour.Properties properties) {
        super(properties
                .lightLevel(blockState -> 15)
                .destroyTime(1)
                .explosionResistance(3600));
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        dropTableContents(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void dropTableContents(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FabricEnchantingCustomTableBlockEntity table) {
            dropInventorySlots(level, pos, table.getInventory(), FabricEnchantingCustomMenu.TOOL_SLOT);
            level.updateNeighbourForOutputSignal(pos, this);
        } else if (blockEntity instanceof FabricEnchantmentConversionTableBlockEntity table) {
            dropInventorySlots(
                    level,
                    pos,
                    table.getInventory(),
                    FabricEnchantmentConversionMenu.BOOK_SLOT,
                    FabricEnchantmentConversionMenu.PAYMENT_SLOT,
                    FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT,
                    FabricEnchantmentConversionMenu.COPY_RESULT_SLOT
            );
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }

    private void dropInventorySlots(Level level, BlockPos pos, FabricTableInventory inventory, int... slots) {
        for (int slot : slots) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? (lvl, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof FabricEnchantingTableLikeBlockEntity enchantingTable) {
                FabricEnchantingTableLikeBlockEntity.bookAnimationTick(lvl, pos, blockState, enchantingTable);
            }
        } : null;
    }

    @Override
    public abstract @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state);
}
