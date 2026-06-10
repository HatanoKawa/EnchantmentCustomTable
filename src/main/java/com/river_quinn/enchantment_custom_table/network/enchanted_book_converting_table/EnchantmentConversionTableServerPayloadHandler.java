package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.server.level.ServerPlayer;

public final class EnchantmentConversionTableServerPayloadHandler {
    private EnchantmentConversionTableServerPayloadHandler() {
    }

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final ServerPlayer player) {
        if (player == null || !(player.containerMenu instanceof EnchantmentConversionMenu menu) || !menu.stillValid(player)) {
            return;
        }

        if (!data.intent().dispatchTo(menu, data.searchQuery(), data.clientLanguage(), data.matchedEnchantments())) {
            return;
        }
        menu.broadcastChanges();
    }
}
