package com.river_quinn.enchantment_custom_table;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.init.ModBlockEntityRenderers;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModItems;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.init.ModPayloads;
import com.river_quinn.enchantment_custom_table.init.ModScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(EnchantmentCustomTable.MODID)
public class EnchantmentCustomTable {
    public static final String MODID = "enchantment_custom_table";

    public EnchantmentCustomTable() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        ModPayloads.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ModBlockEntityRenderers::register);
            modEventBus.addListener(ModScreens::register);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK);
            event.accept(ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK);
        }
    }
}
