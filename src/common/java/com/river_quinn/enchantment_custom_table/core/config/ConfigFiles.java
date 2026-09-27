package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.Consumer;

public final class ConfigFiles {
    private ConfigFiles() {}

    public static void backupMigration(Path file) throws IOException {
        Path backup = file.resolveSibling(file.getFileName() + ".payment-v1.bak");
        if (Files.exists(file) && !Files.exists(backup)) Files.copy(file, backup);
    }

    public static void writeAtomic(Path path, String content) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    public static TableConfigSnapshot loadJson(Path path, Consumer<String> warning) throws IOException {
        if (!Files.exists(path)) {
            writeJson(path, JsonTableConfigCodec.defaultJson());
            return JsonTableConfigCodec.defaultSnapshot();
        }
        JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        TableConfigSnapshot parsed = JsonTableConfigCodec.parse(root, warning);
        if (JsonTableConfigCodec.needsMigration(root)) {
            backupMigration(path);
            root.add(JsonTableConfigCodec.PAYMENT_OPTIONS,
                    JsonTableConfigCodec.toJson(parsed).get(JsonTableConfigCodec.PAYMENT_OPTIONS));
            root.addProperty("configVersion", 2);
            root.remove(JsonTableConfigCodec.MINIMUM_EMERALD_COST);
            root.remove(JsonTableConfigCodec.MINIMUM_EMERALD_BLOCK_COST);
            writeJson(path, root);
        }
        return parsed;
    }

    public static void writeJson(Path path, JsonObject root) throws IOException {
        writeAtomic(path, new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n");
    }
}
