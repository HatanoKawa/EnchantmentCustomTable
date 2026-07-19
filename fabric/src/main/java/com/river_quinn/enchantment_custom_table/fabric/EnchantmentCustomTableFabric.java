package com.river_quinn.enchantment_custom_table.fabric;

import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlocks;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModItems;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import com.river_quinn.enchantment_custom_table.fabric.network.FabricModPayloads;
import com.river_quinn.enchantment_custom_table.fabric.transfer.FabricItemStorageAdapters;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentCustomTableFabric implements ModInitializer {
    public static final String MODID = "enchantment_custom_table";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        FabricTableConfig.load();
        FabricModBlocks.register();
        FabricModItems.register();
        FabricModBlockEntities.register();
        FabricModMenus.register();
        FabricModPayloads.register();
        FabricItemStorageAdapters.register();
        LOGGER.info("Initialized EnchantmentCustomTable Fabric");
    }

}
