package com.river_quinn.enchantment_custom_table.init;

import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<BlockItem> ENCHANTMENT_CUSTOM_TABLE_ITEM = ITEMS.registerSimpleBlockItem(
            "enchanting_custom_table",
            ModBlocks.ENCHANTMENT_CUSTOM_TABLE_BLOCK
    );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
