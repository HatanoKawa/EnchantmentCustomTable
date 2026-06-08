package com.river_quinn.enchantment_custom_table.fabric.block;

import com.mojang.serialization.MapCodec;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FabricEnchantmentConversionTableBlock extends FabricEnchantingTableLikeBlock {
    public static final MapCodec<FabricEnchantmentConversionTableBlock> CODEC = simpleCodec(properties -> new FabricEnchantmentConversionTableBlock());

    public FabricEnchantmentConversionTableBlock() {
        super(FabricVersionedMinecraft.blockProperties("enchantment_conversion_table"));
    }

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
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            FabricVersionedMinecraft.openBlockPosMenu(
                    serverPlayer,
                    Component.translatable("block.enchantment_custom_table.enchantment_conversion_table"),
                    pos,
                    FabricEnchantmentConversionMenu::new
            );
        }
        return FabricVersionedMinecraft.sidedSuccess(level);
    }
}
