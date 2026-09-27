package com.river_quinn.enchantment_custom_table.network;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigWireCodec;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.network.PacketDistributor;

public final class TableConfigSync {
    private static volatile MinecraftServer server;
    private static long revision;
    private TableConfigSync() {}
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(TableConfigSync::starting);
        MinecraftForge.EVENT_BUS.addListener(TableConfigSync::stopped);
        MinecraftForge.EVENT_BUS.addListener(TableConfigSync::joined);
    }
    private static void starting(ServerAboutToStartEvent event) {
        server = event.getServer();
        Config.resolveLocal();
        revision++;
    }
    private static void stopped(ServerStoppedEvent event) { server = null; }
    private static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player);
    }
    private static void send(ServerPlayer player) {
        com.river_quinn.enchantment_custom_table.init.ModPayloads.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TableConfigPayload(TableConfigWireCodec.encode(revision, Config.snapshot())));
    }
    public static void configChanged() {
        MinecraftServer current = server;
        if (current == null) return; // Item registries/components may not yet be ready during initial config loading.
        current.execute(() -> {
            if (server != current) return;
            Config.resolveLocal(); revision++;
            for (ServerPlayer player : current.getPlayerList().getPlayers()) {
                send(player);
                if (player.containerMenu instanceof EnchantmentConversionMenu menu) menu.regenerateEnchantedBookSlot();
                player.containerMenu.broadcastChanges();
            }
        });
    }
}
