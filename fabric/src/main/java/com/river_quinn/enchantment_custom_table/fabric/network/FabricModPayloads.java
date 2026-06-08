package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricModPayloads {
    private FabricModPayloads() {
    }

    public static void register() {
        FabricVersionedMinecraft.registerServerboundPlayPayload(FabricConversionSearchPayload.TYPE, FabricConversionSearchPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FabricConversionSearchPayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof FabricEnchantmentConversionMenu menu) {
                ConversionTableIntent.SEARCH.dispatchTo(
                        menu,
                        payload.query(),
                        payload.clientLanguage(),
                        payload.matchedEnchantments()
                );
            }
        });
    }
}
