package com.river_quinn.enchantment_custom_table.fabric.block;

import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FabricEnchantingCustomTableBlock extends FabricEnchantingTableLikeBlock {
    public FabricEnchantingCustomTableBlock() {
        super(FabricVersionedMinecraft.blockProperties("enchanting_custom_table"));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FabricEnchantingCustomTableBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            FabricVersionedMinecraft.openBlockPosMenu(
                    serverPlayer,
                    Component.translatable("block.enchantment_custom_table.enchanting_custom_table"),
                    pos,
                    FabricEnchantingCustomMenu::new
            );
        }
        return FabricVersionedMinecraft.sidedSuccess(level);
    }
}
