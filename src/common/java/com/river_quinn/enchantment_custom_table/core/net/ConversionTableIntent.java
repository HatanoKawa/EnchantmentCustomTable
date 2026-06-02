package com.river_quinn.enchantment_custom_table.core.net;

import java.util.Arrays;
import java.util.List;

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

    public boolean dispatchTo(
            ConversionTableActions actions,
            String searchQuery,
            String clientLanguage,
            List<String> matchedEnchantments
    ) {
        if (actions == null) {
            return false;
        }

        switch (this) {
            case NEXT_PAGE -> actions.nextPage();
            case PREVIOUS_PAGE -> actions.previousPage();
            case SEARCH -> actions.setSearchQuery(searchQuery, clientLanguage, matchedEnchantments);
            case UNKNOWN -> {
                return false;
            }
        }
        return true;
    }
}
