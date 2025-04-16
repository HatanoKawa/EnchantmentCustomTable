package com.river_quinn.enchantment_custom_table.block;

import com.mojang.serialization.MapCodec;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EnchantingCustomTableBlock extends EnchantingTableLikeBlock {
    public static final MapCodec<EnchantingCustomTableBlock> CODEC = simpleCodec(EnchantingCustomTableBlock::new);

    public EnchantingCustomTableBlock(Properties properties) {
        super(properties);
    }

    public EnchantingCustomTableBlock(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected MapCodec<? extends EnchantingCustomTableBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new EnchantingCustomTableBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player entity, BlockHitResult result) {
        super.useWithoutItem(state, level, pos, entity, result);

        if (entity instanceof ServerPlayer player) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal("Enchanting Custom Table Block");
                }

                @Override
                public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new EnchantingCustomMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
                }
            }, pos);
        }
        return InteractionResult.SUCCESS;

    }

//    @Override
//    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
//        if (state.getBlock() != newState.getBlock()) {
//            BlockEntity blockEntity = world.getBlockEntity(pos);
//            if (blockEntity instanceof EnchantingCustomTableBlockEntity be) {
//                // Containers.dropContents(world, pos, be);
////                be.dropToolInFirstSlotOnRemove();
//                world.updateNeighbourForOutputSignal(pos, this);
//            }
//            super.onRemove(state, world, pos, newState, isMoving);
//        }
//    }


}
