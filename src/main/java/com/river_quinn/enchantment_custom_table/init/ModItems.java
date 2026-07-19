package com.river_quinn.enchantment_custom_table.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<BlockItem> ENCHANTMENT_CUSTOM_TABLE_ITEM = ITEMS.register(
            "enchanting_custom_table",
            () -> new BlockItem(
                    ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)
            )
    );

    public static final RegistryObject<BlockItem> ENCHANTMENT_CONVERSION_TABLE_ITEM = ITEMS.register(
            "enchantment_conversion_table",
            () -> new BlockItem(
                    ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get(),
                    new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)
            )
    );

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
