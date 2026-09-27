package com.river_quinn.enchantment_custom_table.core.config;

import com.google.gson.*;

/** Small, versioned wire representation; malformed network input must not become default pricing. */
public final class TableConfigWireCodec {
    public static final int MAX_LENGTH = 131072;
    public record Update(long revision, TableConfigSnapshot config) {}
    private TableConfigWireCodec() {}

    public static String encode(long revision, TableConfigSnapshot snapshot) {
        JsonObject root = JsonTableConfigCodec.toJson(snapshot);
        root.addProperty("revision", revision);
        String result = root.toString();
        if (result.length() > MAX_LENGTH) throw new IllegalArgumentException("Payment config exceeds network size limit");
        return result;
    }

    public static Update decode(String input) {
        if (input.length() > MAX_LENGTH) throw new IllegalArgumentException("Oversized config packet");
        JsonObject root = JsonParser.parseString(input).getAsJsonObject();
        if (root.get("configVersion").getAsInt() != 2) throw new IllegalArgumentException("Unsupported config protocol");
        JsonArray entries = root.getAsJsonArray(JsonTableConfigCodec.PAYMENT_OPTIONS);
        if (entries == null || entries.size() > PaymentOptions.MAX_ENTRIES) throw new IllegalArgumentException("Invalid payment list");
        // Empty is deliberately accepted only for an unavailable server configuration.
        TableConfigSnapshot snapshot = entries.isEmpty() ? TableConfigState.UNAVAILABLE
                : JsonTableConfigCodec.parse(root, problem -> { throw new IllegalArgumentException(problem); });
        if (!entries.isEmpty() && snapshot.paymentOptions().size() != entries.size()) throw new IllegalArgumentException("Duplicate payments");
        return new Update(root.get("revision").getAsBigDecimal().longValueExact(), snapshot);
    }
}
