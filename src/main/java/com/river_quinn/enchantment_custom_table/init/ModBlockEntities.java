package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.river_quinn.enchantment_custom_table.EnchantmentCustomTable.MODID;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<BlockEntityType<EnchantingCustomTableBlockEntity>> ENCHANTING_CUSTOM_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "enchanting_custom_table",
                    () -> BlockEntityType.Builder.of(
                            EnchantingCustomTableBlockEntity::new,
                            ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get()
                    ).build(null)
            );

    public static final RegistryObject<BlockEntityType<EnchantmentConversionTableBlockEntity>> ENCHANTMENT_CONVERSION_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "enchantment_conversion_table",
                    () -> BlockEntityType.Builder.of(
                            EnchantmentConversionTableBlockEntity::new,
                            ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
