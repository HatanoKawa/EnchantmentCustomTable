package com.river_quinn.enchantment_custom_table.client;

import com.river_quinn.enchantment_custom_table.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

public final class ClientTableConfigSync {
    private ClientTableConfigSync() {}
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggedInEvent event) -> Config.STATE.beginConnection());
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggedOutEvent event) -> Config.STATE.disconnect());
    }
}
