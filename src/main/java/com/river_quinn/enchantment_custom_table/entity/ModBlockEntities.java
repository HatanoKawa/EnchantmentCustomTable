package com.river_quinn.enchantment_custom_table.entity;

import com.river_quinn.enchantment_custom_table.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<EnchantingCustomTableBlockEntity>> ENCHANTING_CUSTOM_TABLE = BLOCK_ENTITY_TYPES.register(
            "enchanting_custom_table",
            () -> BlockEntityType.Builder.of(EnchantingCustomTableBlockEntity::new,
                    ModBlocks.ENCHANTMENT_CUSTOM_TABLE_BLOCK.get()).build(null)
    );

    public static void register(IEventBus eventBus){
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
