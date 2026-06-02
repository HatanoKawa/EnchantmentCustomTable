package com.river_quinn.enchantment_custom_table.fabric.block;

import com.mojang.serialization.MapCodec;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FabricEnchantingCustomTableBlock extends FabricEnchantingTableLikeBlock {
    public static final MapCodec<FabricEnchantingCustomTableBlock> CODEC = simpleCodec(properties -> new FabricEnchantingCustomTableBlock());

    @Override
    protected MapCodec<? extends FabricEnchantingCustomTableBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FabricEnchantingCustomTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayer player) {
                    return pos;
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable("block.enchantment_custom_table.enchanting_custom_table");
                }

                @Override
                public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                    return new FabricEnchantingCustomMenu(id, inventory, pos);
                }
            });
        }
        return FabricVersionedMinecraft.sidedSuccess(level);
    }
}
