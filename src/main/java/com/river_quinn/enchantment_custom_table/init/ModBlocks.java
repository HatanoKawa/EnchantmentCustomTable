package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.block.EnchantingCustomTableBlock;
import com.river_quinn.enchantment_custom_table.block.EnchantmentConversionTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> ENCHANTING_CUSTOM_TABLE_BLOCK = BLOCKS.register(
            "enchanting_custom_table",
            EnchantingCustomTableBlock::new
    );

    public static final RegistryObject<Block> ENCHANTMENT_CONVERSION_TABLE_BLOCK = BLOCKS.register(
            "enchantment_conversion_table",
            EnchantmentConversionTableBlock::new
    );

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
