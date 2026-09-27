package com.river_quinn.enchantment_custom_table.fabric.client;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.network.FabricTableConfigSync;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientTableConfigSync {
    private ClientTableConfigSync() {}
    public static void register() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> FabricTableConfig.STATE.beginConnection());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FabricTableConfig.STATE.disconnect());
        ClientPlayNetworking.registerGlobalReceiver(FabricTableConfigSync.ID, (client, handler, buffer, sender) -> {
            var update = TableConfigWireCodec.decode(buffer.readUtf(TableConfigWireCodec.MAX_LENGTH));
            client.execute(() -> {
                if (client.getConnection() == handler) FabricTableConfig.STATE.receive(update.revision(), update.config());
            });
        });
    }
}
