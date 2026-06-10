package com.river_quinn.enchantment_custom_table.network.enchanting_custom_table;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.minecraft.server.level.ServerPlayer;

public final class EnchantingCustomTableServerPayloadHandler {
    private EnchantingCustomTableServerPayloadHandler() {
    }

    public static void handleDataOnMain(final EnchantingCustomTableNetData data, final ServerPlayer player) {
        if (player == null || !(player.containerMenu instanceof EnchantingCustomMenu menu) || !menu.stillValid(player)) {
            return;
        }

        if (!data.intent().dispatchTo(menu)) {
            return;
        }
        menu.broadcastChanges();
    }
}
