package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantmentConversionTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final IPayloadContext context) {
        BlockPos blockPos = new BlockPos(data.blockPosX(), data.blockPosY(), data.blockPosZ());
        EnchantmentConversionTableBlockEntity blockEntity =
                (EnchantmentConversionTableBlockEntity)context.player().level().getBlockEntity(blockPos);

        switch (EnchantmentConversionTableNetData.OperateType.valueOf(data.operateType())) {
            case NEXT_PAGE -> {
                blockEntity.nextPage();
            }
            case PREVIOUS_PAGE -> {
                blockEntity.previousPage();
            }
        }
    }
}
