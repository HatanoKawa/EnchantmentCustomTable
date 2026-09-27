package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

public final class FabricTableConfigSync {
    private FabricTableConfigSync() {}
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(FabricTableConfigPayload.TYPE, FabricTableConfigPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricTableConfig.load();
            FabricTableConfig.resolveLocal();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!ServerPlayNetworking.canSend(handler.player, FabricTableConfigPayload.TYPE)) {
                handler.disconnect(Component.literal("EnchantmentCustomTable requires matching payment-config protocol v2 on the client."));
                return;
            }
            ServerPlayNetworking.send(handler.player, new FabricTableConfigPayload(TableConfigWireCodec.encode(0, FabricTableConfig.snapshot())));
        });
    }
}
