package com.river_quinn.enchantment_custom_table.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private static TableConfigSnapshot snapshot = new TableConfigSnapshot(36, 4, false, false, false);

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
            snapshot = new TableConfigSnapshot(
                    intValue(root, "minimumEmeraldCost", 36),
                    intValue(root, "minimumEmeraldBlockCost", 4),
                    booleanValue(root, "enforceEnchantmentLevelLimit", false),
                    booleanValue(root, "incrementalSameLevelMerge", false),
                    booleanValue(root, "convertOnlyLevelOneBook", false)
            );
            warnIfDeprecatedChanged(root, "ignoreEnchantmentLevelLimit", true, "enforceEnchantmentLevelLimit");
            warnIfDeprecatedChanged(root, "convert_max_level_book", true, "convertOnlyLevelOneBook");
        } catch (RuntimeException | IOException exception) {
            EnchantmentCustomTableFabric.LOGGER.warn("Failed to load Fabric config {}, using defaults", configPath, exception);
            snapshot = new TableConfigSnapshot(36, 4, false, false, false);
        }
    }

    public static TableConfigSnapshot snapshot() {
        return snapshot;
    }

    private static void saveDefault(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("minimumEmeraldCost", 36);
            root.addProperty("minimumEmeraldBlockCost", 4);
            root.addProperty("enforceEnchantmentLevelLimit", false);
            root.addProperty("incrementalSameLevelMerge", false);
            root.addProperty("convertOnlyLevelOneBook", false);
            root.addProperty("ignoreEnchantmentLevelLimit", true);
            root.addProperty("convert_max_level_book", true);
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            EnchantmentCustomTableFabric.LOGGER.warn("Failed to write default Fabric config {}", configPath, exception);
        }
    }

    private static int intValue(JsonObject root, String name, int defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsInt() : defaultValue;
    }

    private static boolean booleanValue(JsonObject root, String name, boolean defaultValue) {
        return root.has(name) && root.get(name).isJsonPrimitive() ? root.get(name).getAsBoolean() : defaultValue;
    }

    private static void warnIfDeprecatedChanged(JsonObject root, String name, boolean defaultValue, String replacementName) {
        if (root.has(name) && root.get(name).isJsonPrimitive() && root.get(name).getAsBoolean() != defaultValue) {
            EnchantmentCustomTableFabric.LOGGER.warn(
                    "Config option '{}' is deprecated and ignored. Use '{}' instead.",
                    name,
                    replacementName
            );
        }
    }
}
