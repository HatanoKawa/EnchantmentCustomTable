package com.river_quinn.enchantment_custom_table.core.platform;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;

@FunctionalInterface
public interface TableConfigService {
    TableConfigView snapshot();
}
