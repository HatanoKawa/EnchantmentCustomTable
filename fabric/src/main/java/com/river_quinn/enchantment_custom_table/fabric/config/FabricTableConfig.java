package com.river_quinn.enchantment_custom_table.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.river_quinn.enchantment_custom_table.core.config.JsonTableConfigCodec;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FabricTableConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = EnchantmentCustomTableFabric.MODID + ".json";
    private static TableConfigSnapshot snapshot = JsonTableConfigCodec.defaultSnapshot();

    private FabricTableConfig() {
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        if (!Files.exists(configPath)) {
            saveDefault(configPath);
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            snapshot = JsonTableConfigCodec.parse(root);
        } catch (RuntimeException | IOException exception) {
            EnchantmentCustomTableFabric.LOGGER.warn("Failed to load Fabric config {}, using defaults", configPath, exception);
            snapshot = JsonTableConfigCodec.defaultSnapshot();
        }
    }

    public static TableConfigSnapshot snapshot() {
        return snapshot;
    }

    private static void saveDefault(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(JsonTableConfigCodec.defaultJson(), writer);
            }
        } catch (IOException exception) {
            EnchantmentCustomTableFabric.LOGGER.warn("Failed to write default Fabric config {}", configPath, exception);
        }
    }
}
