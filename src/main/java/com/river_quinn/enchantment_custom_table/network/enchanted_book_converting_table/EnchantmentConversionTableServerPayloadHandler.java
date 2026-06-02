package com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EnchantmentConversionTableServerPayloadHandler {

    public static void handleDataOnMain(final EnchantmentConversionTableNetData data, final IPayloadContext context) {
        if (!(context.player().containerMenu instanceof EnchantmentConversionMenu menu) || !menu.stillValid(context.player())) {
            return;
        }

        ConversionTableIntent intent = data.intent();
        if (intent == ConversionTableIntent.UNKNOWN) {
            return;
        }

        switch (intent) {
            case NEXT_PAGE -> {
                menu.nextPage();
            }
            case PREVIOUS_PAGE -> {
                menu.previousPage();
            }
            case SEARCH -> {
                menu.setSearchQuery(data.searchQuery(), data.clientLanguage(), data.matchedEnchantments());
            }
            case UNKNOWN -> {
                return;
            }
        }
        menu.broadcastChanges();
    }
}
