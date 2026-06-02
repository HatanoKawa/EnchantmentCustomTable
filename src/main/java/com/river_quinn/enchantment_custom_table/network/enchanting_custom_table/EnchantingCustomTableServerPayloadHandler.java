package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantingCustomTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantingCustomTableNetData data, final IPayloadContext context) {
        if (!(context.player().containerMenu instanceof EnchantingCustomMenu menu) || !menu.stillValid(context.player())) {
            return;
        }

        EnchantingTableIntent intent = data.intent();
        if (intent == EnchantingTableIntent.UNKNOWN) {
            return;
        }

        switch (intent) {
            case EXPORT_ALL_ENCHANTMENTS -> {
                menu.exportAllEnchantments();
            }
            case NEXT_PAGE -> {
                menu.nextPage();
            }
            case PREVIOUS_PAGE -> {
                menu.previousPage();
            }
            case UNKNOWN -> {
                return;
            }
        }
        menu.broadcastChanges();
    }
}
