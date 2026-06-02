package com.river_quinn.enchantment_custom_table.core.net;

import java.util.Arrays;

public enum ConversionTableIntent {
    UNKNOWN("unknown"),
    NEXT_PAGE("next_page"),
    PREVIOUS_PAGE("previous_page"),
    SEARCH("search");

    private final String networkId;

    ConversionTableIntent(String networkId) {
        this.networkId = networkId;
    }

    public String networkId() {
        return networkId;
    }

    public static ConversionTableIntent fromNetworkId(String networkId) {
        if (networkId == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(intent -> intent.networkId.equals(networkId))
                .findFirst()
                .orElseGet(() -> fromLegacyName(networkId));
    }

    public static ConversionTableIntent fromLegacyName(String legacyName) {
        if (legacyName == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(legacyName);
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }
}
