package com.river_quinn.enchantment_custom_table.init;

import com.river_quinn.enchantment_custom_table.EnchantmentCustomTable;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import java.util.List;

public final class ModSpriteAtlases {
    private static final List<ResourceLocation> BLOCK_ATLAS_SPRITES = List.of(
            sprite("item/empty_slot_book"),
            sprite("item/empty_slot_book_disabled"),
            sprite("item/empty_slot_emerald"),
            sprite("item/copy_template_slot"),
            sprite("item/output_slot"),
            sprite("entity/enchanting_custom_table_book"),
            sprite("entity/enchantment_conversion_table_book")
    );

    private ModSpriteAtlases() {
    }

    public static void stitch(TextureStitchEvent.Pre event) {
        if (!TextureAtlas.LOCATION_BLOCKS.equals(event.getAtlas().location())) {
            return;
        }
        BLOCK_ATLAS_SPRITES.forEach(event::addSprite);
    }

    private static ResourceLocation sprite(String path) {
        return new ResourceLocation(EnchantmentCustomTable.MODID, path);
    }
}
