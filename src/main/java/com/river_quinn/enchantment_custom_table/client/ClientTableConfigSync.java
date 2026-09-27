package com.river_quinn.enchantment_custom_table.client;

import com.river_quinn.enchantment_custom_table.Config;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public final class ClientTableConfigSync {
    private ClientTableConfigSync() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> Config.STATE.beginConnection());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> Config.STATE.disconnect());
    }
}
