package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PaymentOptionsTest {
    @TempDir Path directory;
    private TableConfigSnapshot parse(String entries) {
        return JsonTableConfigCodec.parse(JsonParser.parseString("{\"paymentOptions\":" + entries + "}").getAsJsonObject());
    }
    @Test void threeDefaultsAndExplicitListOverridesLegacy() {
        assertEquals(3, JsonTableConfigCodec.defaultSnapshot().paymentOptions().size());
        var root = JsonParser.parseString("{\"minimumEmeraldCost\":1,\"paymentOptions\":[{\"item_id\":\"minecraft:nether_star\",\"cost\":8}]}").getAsJsonObject();
        assertEquals(Map.of("minecraft:nether_star", 8), JsonTableConfigCodec.parse(root).paymentOptions());
    }
    @Test void excludesBothBooksInvalidIdsAndBadTypesButCorrectsIntegers() {
        var snapshot = parse("[{\"item_id\":\"minecraft:book\",\"cost\":1},"
                + "{\"item_id\":\"minecraft:enchanted_book\",\"cost\":1},"
                + "{\"item_id\":\"minecraft:air\",\"cost\":1},"
                + "{\"item_id\":\"bad id\",\"cost\":1},"
                + "{\"item_id\":\"minecraft:diamond\",\"cost\":\"5\"},"
                + "{\"item_id\":\"minecraft:diamond\",\"cost\":1.5},"
                + "{\"item_id\":\"minecraft:nether_star\",\"cost\":0},"
                + "{\"item_id\":\"minecraft:nether_star\",\"cost\":8}]");
        assertEquals(Map.of("minecraft:nether_star", 1), snapshot.paymentOptions());
        assertEquals(PaymentOptions.DEFAULTS, parse("[{\"item_id\":\"minecraft:book\",\"cost\":1}]").paymentOptions());
        assertEquals(PaymentOptions.DEFAULTS, parse("[]").paymentOptions());
    }
    @Test void hugeIntegersDoNotOverflowIntoCheapPrices() {
        assertEquals(64, parse("[{\"item_id\":\"minecraft:diamond\",\"cost\":999999999999999999999999999999}]").paymentCost("minecraft:diamond"));
        assertEquals(1, parse("[{\"item_id\":\"minecraft:diamond\",\"cost\":-999999999999999999999999999999}]").paymentCost("minecraft:diamond"));
        assertEquals(16, PaymentOptions.clamp(BigInteger.valueOf(36), 16));
        assertEquals(1, PaymentOptions.clamp(BigInteger.valueOf(36), 1));
    }
    @Test void registryFiltersMissingItemsThenUsesDefaultsOnlyWhenEmpty() {
        var limits = Map.of("minecraft:emerald",64,"minecraft:emerald_block",64,"minecraft:nether_star",64,"pack:token",16);
        assertEquals(Map.of("pack:token",16), PaymentOptions.resolve(Map.of("pack:token",36,"missing:item",4), id -> limits.getOrDefault(id,0), s -> {}));
        assertEquals(PaymentOptions.DEFAULTS, PaymentOptions.resolve(Map.of("missing:item",4), id -> limits.getOrDefault(id,0), s -> {}));
    }
    @Test void legacyZeroIsOmittedAndMigrationIsBackedUpAndIdempotent() throws Exception {
        Path path = directory.resolve("config.json");
        String original = "{\"minimumEmeraldCost\":0,\"minimumEmeraldBlockCost\":7,\"freeConversionTableCosts\":true,\"unrelated\":42}";
        Files.writeString(path, original);
        var first = ConfigFiles.loadJson(path, s -> {});
        assertEquals(Map.of("minecraft:emerald_block",7),first.paymentOptions());
        assertTrue(first.freeConversionTableCosts());
        assertEquals(original,Files.readString(directory.resolve("config.json.payment-v1.bak")));
        String upgraded = Files.readString(path);
        assertTrue(upgraded.contains("unrelated"));
        assertFalse(upgraded.contains("minimumEmeraldCost"));
        assertEquals(first,ConfigFiles.loadJson(path,s -> {}));
        assertEquals(upgraded,Files.readString(path));
        assertEquals(PaymentOptions.DEFAULTS, PaymentOptions.legacy(0,0));
    }
    @Test void brokenFileRemainsIntact() throws Exception {
        Path path=directory.resolve("bad.json");Files.writeString(path,"{ invalid");
        assertThrows(RuntimeException.class,()->ConfigFiles.loadJson(path,s -> {}));
        assertEquals("{ invalid",Files.readString(path));
    }
    @Test void networkViewCannotOverrideServerOrLeakIntoNextConnection() {
        var state=new TableConfigState();var local=JsonTableConfigCodec.defaultSnapshot();
        var remote=new TableConfigSnapshot(Map.of("minecraft:nether_star",8),true,true,true,true);
        state.setLocal(local);state.beginConnection();
        assertEquals(TableConfigState.UNAVAILABLE,state.forSide(true));
        var decoded=TableConfigWireCodec.decode(TableConfigWireCodec.encode(2,remote));
        state.receive(decoded.revision(),decoded.config());
        state.receive(1,local);
        assertEquals(remote,state.forSide(true));assertEquals(local,state.forSide(false));
        state.disconnect();assertEquals(local,state.forSide(true));
        state.beginConnection();assertEquals(TableConfigState.UNAVAILABLE,state.forSide(true));
        state.disconnect();state.receive(3,remote);assertEquals(local,state.forSide(true));
    }
    @Test void invalidWireDataDoesNotFallbackToDefaults() {
        String invalid=TableConfigWireCodec.encode(1,JsonTableConfigCodec.defaultSnapshot()).replace("\"cost\":36","\"cost\":0");
        assertThrows(IllegalArgumentException.class,()->TableConfigWireCodec.decode(invalid));
    }
}
