package com.river_quinn.enchantment_custom_table.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.*;
import com.river_quinn.enchantment_custom_table.core.config.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

public final class TomlPaymentConfig {
    private TomlPaymentConfig() {}

    public static List<CommentedConfig> entries(Map<String, Integer> payments) {
        List<CommentedConfig> entries = new ArrayList<>();
        payments.forEach((id, cost) -> {
            CommentedConfig entry = CommentedConfig.inMemory();
            entry.set("item_id", id); entry.set("cost", cost); entries.add(entry);
        });
        return entries;
    }

    public static Map<String, Integer> read(Object value, Consumer<String> warning) {
        return JsonTableConfigCodec.readPayments(json(value), warning);
    }

    public static TableConfigSnapshot load(Path path, Consumer<String> warning) throws IOException {
        return JsonTableConfigCodec.parse(json(new TomlParser().parse(Files.readString(path))).getAsJsonObject(), warning);
    }

    public static void save(Path path, TableConfigSnapshot value) throws IOException {
        // Parse before writing: an unfinished manual edit must never be replaced with defaults.
        CommentedConfig config = Files.exists(path) ? new TomlParser().parse(Files.readString(path)) : CommentedConfig.inMemory();
        config.set("configVersion", 2);
        config.set("paymentOptions", entries(value.paymentOptions()));
        config.set("enforceEnchantmentLevelLimit", value.enforceEnchantmentLevelLimit());
        config.set("incrementalSameLevelMerge", value.incrementalSameLevelMerge());
        config.set("convertOnlyLevelOneBook", value.convertOnlyLevelOneBook());
        config.set("freeConversionTableCosts", value.freeConversionTableCosts());
        config.remove(JsonTableConfigCodec.MINIMUM_EMERALD_COST);
        config.remove(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST);
        ConfigFiles.writeAtomic(path, new TomlWriter().writeToString(config));
    }

    /** Runs before registering the spec: framework correction otherwise deletes the legacy keys. */
    public static void migrate(Path path, Consumer<String> warning) {
        if (!Files.exists(path)) return;
        try {
            CommentedConfig config = new TomlParser().parse(Files.readString(path));
            JsonObject root = json(config).getAsJsonObject();
            if (!JsonTableConfigCodec.needsMigration(root)) return;
            TableConfigSnapshot converted = JsonTableConfigCodec.parse(root, warning);
            ConfigFiles.backupMigration(path);
            config.set(JsonTableConfigCodec.PAYMENT_OPTIONS, entries(converted.paymentOptions()));
            config.set("configVersion", 2);
            config.remove(JsonTableConfigCodec.MINIMUM_EMERALD_COST);
            config.remove(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST);
            ConfigFiles.writeAtomic(path, new TomlWriter().writeToString(config));
        } catch (IOException | RuntimeException ex) {
            throw new IllegalStateException("Cannot safely read/migrate " + path + "; original file retained", ex);
        }
    }

    private static JsonElement json(Object value) {
        if (value instanceof UnmodifiableConfig config) {
            JsonObject object = new JsonObject();
            config.entrySet().forEach(entry -> object.add(entry.getKey(), json(entry.getValue())));
            return object;
        }
        if (value instanceof List<?> list) {
            JsonArray array = new JsonArray(); list.forEach(entry -> array.add(json(entry))); return array;
        }
        if (value instanceof Number number) return new JsonPrimitive(number);
        if (value instanceof Boolean bool) return new JsonPrimitive(bool);
        if (value instanceof String string) return new JsonPrimitive(string);
        return JsonNull.INSTANCE;
    }
}
