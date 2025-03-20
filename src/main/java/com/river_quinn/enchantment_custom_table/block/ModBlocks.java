package com.river_quinn.enchantment_custom_table.block;

import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<Block> ENCHANTMENT_CUSTOM_TABLE_BLOCK = BLOCKS.register(
            "enchanting_custom_table",
            registryName -> new EnchantingCustomTableBlock()
    );

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
