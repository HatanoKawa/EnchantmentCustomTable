package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantingCustomTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantingCustomTableNetData data, final IPayloadContext context) {
        if (!(context.player().containerMenu instanceof EnchantingCustomMenu menu) || !menu.stillValid(context.player())) {
            return;
        }

        if (!data.intent().dispatchTo(menu)) {
            return;
        }
        menu.broadcastChanges();
    }
}
