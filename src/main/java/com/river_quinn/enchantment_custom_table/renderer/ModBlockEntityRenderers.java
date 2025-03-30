package com.river_quinn.enchantment_custom_table.renderer;

import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModBlockEntityRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(),
                EnchantingCustomTableRenderer::new
        );

        event.registerBlockEntityRenderer(
                ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(),
                EnchantingCustomTableRenderer::new
        );
    }
}
