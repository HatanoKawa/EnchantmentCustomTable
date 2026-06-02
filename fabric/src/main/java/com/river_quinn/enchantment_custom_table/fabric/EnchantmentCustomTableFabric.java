package com.river_quinn.enchantment_custom_table.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentCustomTableFabric implements ModInitializer {
    public static final String MODID = "enchantment_custom_table";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing EnchantmentCustomTable Fabric pilot");
    }
}
