package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class FabricModPayloads {
    private FabricModPayloads() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FabricConversionSearchPayload.TYPE, FabricConversionSearchPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FabricConversionSearchPayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof FabricEnchantmentConversionMenu menu) {
                menu.setSearchQuery(payload.query(), payload.clientLanguage(), payload.matchedEnchantments());
            }
        });
    }
}
