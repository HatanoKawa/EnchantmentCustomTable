package com.river_quinn.enchantment_custom_table.fabric;

import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlocks;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
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
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(FabricModItems.ENCHANTING_CUSTOM_TABLE_ITEM);
            entries.accept(FabricModItems.ENCHANTMENT_CONVERSION_TABLE_ITEM);
        });
        LOGGER.info("Initialized EnchantmentCustomTable Fabric pilot");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
