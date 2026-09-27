package com.river_quinn.enchantment_custom_table.fabric.config;

import com.river_quinn.enchantment_custom_table.core.config.*;
import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.io.IOException;

public final class FabricTableConfig {
    public static final TableConfigState STATE = new TableConfigState();
    private static TableConfigSnapshot raw = JsonTableConfigCodec.defaultSnapshot();
    private static boolean loaded;
    private FabricTableConfig() {}
    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(EnchantmentCustomTableFabric.MODID + ".json");
        try {
            raw = ConfigFiles.loadJson(path, FabricTableConfig::warn);
            loaded = true;
        } catch (IOException | RuntimeException ex) {
            EnchantmentCustomTableFabric.LOGGER.error("Cannot load config {}; original retained, keeping last valid rules", path, ex);
        }
    }
    public static void resolveLocal() {
        if (loaded) STATE.setLocal(MinecraftPaymentConfig.resolve(raw, FabricTableConfig::warn));
    }
    private static void warn(String message) { EnchantmentCustomTableFabric.LOGGER.warn(message); }
    public static TableConfigSnapshot snapshot() { return STATE.local(); }
    public static TableConfigSnapshot snapshot(boolean clientSide) { return STATE.forSide(clientSide); }
}
