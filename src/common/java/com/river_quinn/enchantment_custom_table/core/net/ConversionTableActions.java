package com.river_quinn.enchantment_custom_table.core.net;

import java.util.List;

public interface ConversionTableActions {
    void nextPage();

    void previousPage();

    void setSearchQuery(String query, String clientLanguage, List<String> matchedEnchantments);
}
