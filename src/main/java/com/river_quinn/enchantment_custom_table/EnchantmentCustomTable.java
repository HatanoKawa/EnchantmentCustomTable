package com.river_quinn.enchantment_custom_table;

import com.river_quinn.enchantment_custom_table.init.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(EnchantmentCustomTable.MODID)
public class EnchantmentCustomTable
{
    public static final String MODID = "enchantment_custom_table";
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public EnchantmentCustomTable(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(ModPayloads::register);
        modEventBus.addListener(ModCapabilities::register);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //? if >=1.21.9 {
        if (FMLEnvironment.getDist().isClient()) {
        //?} else {
        /*if (FMLEnvironment.dist.isClient()) {
        *///?}
            modEventBus.addListener(ModBlockEntityRenderers::register);
            modEventBus.addListener(ModScreens::register);
            ModConfigScreens.register(modContainer);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK);
            event.accept(ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK);
        }
    }
}
