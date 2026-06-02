package com.river_quinn.enchantment_custom_table.core.net;

import java.util.Arrays;

public enum EnchantingTableIntent {
    UNKNOWN("unknown"),
    EXPORT_ALL_ENCHANTMENTS("export_all_enchantments"),
    NEXT_PAGE("next_page"),
    PREVIOUS_PAGE("previous_page");

    private final String networkId;

    EnchantingTableIntent(String networkId) {
        this.networkId = networkId;
    }

    public String networkId() {
        return networkId;
    }

    public static EnchantingTableIntent fromNetworkId(String networkId) {
        if (networkId == null) {
            return UNKNOWN;
        }
        return Arrays.stream(values())
                .filter(intent -> intent.networkId.equals(networkId))
                .findFirst()
                .orElseGet(() -> fromLegacyName(networkId));
    }

    public static EnchantingTableIntent fromLegacyName(String legacyName) {
        if (legacyName == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(legacyName);
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }

    public boolean dispatchTo(EnchantingTableActions actions) {
        if (actions == null) {
            return false;
        }

        switch (this) {
            case EXPORT_ALL_ENCHANTMENTS -> actions.exportAllEnchantments();
            case NEXT_PAGE -> actions.nextPage();
            case PREVIOUS_PAGE -> actions.previousPage();
            case UNKNOWN -> {
                return false;
            }
        }
        return true;
    }
}
