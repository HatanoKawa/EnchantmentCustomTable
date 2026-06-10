package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricModPayloads {
    private FabricModPayloads() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(FabricConversionSearchPayload.ID, (server, player, handler, buffer, responseSender) -> {
            FabricConversionSearchPayload payload = FabricConversionSearchPayload.read(buffer);
            server.execute(() -> {
                if (!(player.containerMenu instanceof FabricEnchantmentConversionMenu menu)) {
                    return;
                }
                ConversionTableIntent.SEARCH.dispatchTo(
                        menu,
                        payload.query(),
                        payload.clientLanguage(),
                        payload.matchedEnchantments()
                );
            });
        });
    }
}
