package com.river_quinn.enchantment_custom_table.fabric.client;

import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class FabricSpriteAtlases {
    private static final List<ResourceLocation> BLOCK_ATLAS_SPRITES = List.of(
            FabricVersionedMinecraft.id("item/empty_slot_book"),
            FabricVersionedMinecraft.id("item/empty_slot_book_disabled"),
            FabricVersionedMinecraft.id("item/empty_slot_emerald"),
            FabricVersionedMinecraft.id("item/copy_template_slot"),
            FabricVersionedMinecraft.id("item/output_slot"),
            FabricVersionedMinecraft.id("entity/enchanting_custom_table_book"),
            FabricVersionedMinecraft.id("entity/enchantment_conversion_table_book")
    );

    private FabricSpriteAtlases() {
    }

    public static void register() {
        ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlas, registry) ->
                BLOCK_ATLAS_SPRITES.forEach(registry::register)
        );
    }
}
