package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantmentConversionTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final IPayloadContext context) {
        if (!(context.player().containerMenu instanceof EnchantmentConversionMenu menu) || !menu.stillValid(context.player())) {
            return;
        }

        if (!data.intent().dispatchTo(menu, data.searchQuery(), data.clientLanguage(), data.matchedEnchantments())) {
            return;
        }
        menu.broadcastChanges();
    }
}
