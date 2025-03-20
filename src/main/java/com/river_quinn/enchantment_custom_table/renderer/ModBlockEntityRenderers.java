package com.river_quinn.enchantment_custom_table.renderer;

import com.river_quinn.enchantment_custom_table.entity.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModBlockEntityRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                // The block entity type to register the renderer for.
                ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(),
                // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
                EnchantingCustomTableRenderer::new
        );
    }
}
