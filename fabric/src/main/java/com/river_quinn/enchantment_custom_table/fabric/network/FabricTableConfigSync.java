package com.river_quinn.enchantment_custom_table.fabric.network;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FabricTableConfigSync {
    public static final ResourceLocation ID = new ResourceLocation("enchantment_custom_table", "table_config_v2");
    private FabricTableConfigSync() {}
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricTableConfig.load();
            FabricTableConfig.resolveLocal();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!ServerPlayNetworking.canSend(handler.player, ID)) {
                handler.disconnect(new net.minecraft.network.chat.TextComponent("EnchantmentCustomTable requires matching payment-config protocol v2 on the client."));
                return;
            }
            var buffer = PacketByteBufs.create();
            buffer.writeUtf(TableConfigWireCodec.encode(0, FabricTableConfig.snapshot()), TableConfigWireCodec.MAX_LENGTH);
            ServerPlayNetworking.send(handler.player, ID, buffer);
        });
    }
}
