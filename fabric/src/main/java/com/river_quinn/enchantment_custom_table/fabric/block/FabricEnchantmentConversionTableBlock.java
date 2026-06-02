package com.river_quinn.enchantment_custom_table.fabric.block;

import com.mojang.serialization.MapCodec;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FabricEnchantmentConversionTableBlock extends FabricEnchantingTableLikeBlock {
    public static final MapCodec<FabricEnchantmentConversionTableBlock> CODEC = simpleCodec(properties -> new FabricEnchantmentConversionTableBlock());

    @Override
    protected MapCodec<? extends FabricEnchantmentConversionTableBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FabricEnchantmentConversionTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.SUCCESS;
    }
}
