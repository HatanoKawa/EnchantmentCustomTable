package com.river_quinn.enchantment_custom_table.config;

import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.river_quinn.enchantment_custom_table.core.config.PaymentOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TomlPaymentConfigTest {
    @TempDir Path directory;

    @Test void migratesBeforeSpecCorrectionAndPreservesBackupAndFlags() throws Exception {
        Path file = directory.resolve("test.toml");
        String original = "# Existing pack settings\nminimumEmeraldCost = 0\nminimumEmeraldBlockCost = 7\nfreeConversionTableCosts = true\n";
        Files.writeString(file, original);
        TomlPaymentConfig.migrate(file, ignored -> {});
        var migrated = new TomlParser().parse(Files.readString(file));
        assertEquals(Map.of("minecraft:emerald_block", 7), TomlPaymentConfig.read(migrated.get("paymentOptions"), ignored -> {}));
        assertEquals(Boolean.TRUE, migrated.get("freeConversionTableCosts"));
        assertFalse(migrated.contains("minimumEmeraldCost"));
        assertEquals(original, Files.readString(directory.resolve("test.toml.payment-v1.bak")));
        String first = Files.readString(file);
        TomlPaymentConfig.migrate(file, ignored -> {});
        assertEquals(first, Files.readString(file));
    }

    @Test void structuredInlineAndMultilineTablesRoundTripWithoutBecomingDefaults() {
        var input = new TomlParser().parse("paymentOptions = [{item_id = 'minecraft:diamond', cost = 3}, {item_id = 'minecraft:book', cost = 1}]\n");
        assertEquals(Map.of("minecraft:diamond", 3), TomlPaymentConfig.read(input.get("paymentOptions"), ignored -> {}));
        input.set("paymentOptions", TomlPaymentConfig.entries(Map.of("minecraft:nether_star", 5)));
        var reloaded = new TomlParser().parse(new TomlWriter().writeToString(input));
        assertEquals(Map.of("minecraft:nether_star", 5), TomlPaymentConfig.read(reloaded.get("paymentOptions"), ignored -> {}));
    }

    @Test void malformedFileIsUntouchedAndEmptyAndLegacyDisabledListsUseDefaults() throws Exception {
        Path file = directory.resolve("broken.toml");
        String bad = "paymentOptions = [{";
        Files.writeString(file, bad);
        assertThrows(IllegalStateException.class, () -> TomlPaymentConfig.migrate(file, ignored -> {}));
        assertEquals(bad, Files.readString(file));
        assertFalse(Files.exists(directory.resolve("broken.toml.payment-v1.bak")));
        Files.writeString(file, "minimumEmeraldCost = 0\nminimumEmeraldBlockCost = 0\n");
        TomlPaymentConfig.migrate(file, ignored -> {});
        assertEquals(PaymentOptions.DEFAULTS, TomlPaymentConfig.read(new TomlParser().parse(Files.readString(file)).get("paymentOptions"), ignored -> {}));
    }

    @Test void editorSavePreservesOtherFieldsAndRefusesMalformedEdits() throws Exception {
        Path file = directory.resolve("edited.toml");
        Files.writeString(file, "# pack note\nunrelated = 42\n");
        var custom = new com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot(Map.of("minecraft:diamond", 3), true, true, true, false);
        TomlPaymentConfig.save(file, custom);
        assertEquals(custom, TomlPaymentConfig.load(file, ignored -> {}));
        assertEquals(Integer.valueOf(42), new TomlParser().parse(Files.readString(file)).get("unrelated"));
        Files.writeString(file, "paymentOptions = [{");
        assertThrows(RuntimeException.class, () -> TomlPaymentConfig.save(file, custom));
        assertEquals("paymentOptions = [{", Files.readString(file));
    }
}
