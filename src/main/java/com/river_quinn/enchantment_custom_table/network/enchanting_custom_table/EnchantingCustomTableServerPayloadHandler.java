package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantingCustomTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantingCustomTableNetData data, final IPayloadContext context) {
        BlockPos blockPos = new BlockPos(data.blockPosX(), data.blockPosY(), data.blockPosZ());
        EnchantingCustomTableBlockEntity blockEntity =
                (EnchantingCustomTableBlockEntity)context.player().level().getBlockEntity(blockPos);

        switch (EnchantingCustomTableNetData.OperateType.valueOf(data.operateType())) {
            case EXPORT_ALL_ENCHANTMENTS -> {
                blockEntity.exportAllEnchantments(context.player());
            }
            case NEXT_PAGE -> {
                blockEntity.nextPage();
            }
            case PREVIOUS_PAGE -> {
                blockEntity.previousPage();
            }
        }
    }
}
