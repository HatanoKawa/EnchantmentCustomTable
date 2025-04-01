package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantmentConversionTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final IPayloadContext context) {
        EnchantmentConversionMenu menu = (EnchantmentConversionMenu)context.player().containerMenu;

        switch (EnchantmentConversionTableNetData.OperateType.valueOf(data.operateType())) {
            case NEXT_PAGE -> {
                menu.nextPage();
            }
            case PREVIOUS_PAGE -> {
                menu.previousPage();
            }
        }
    }
}
