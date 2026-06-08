package com.river_quinn.enchantment_custom_table.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingTableLikeBlockEntity;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FabricEnchantingCustomTableRenderer<T extends FabricEnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "enchantment/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "enchantment/enchantment_conversion_table_book";

    private final SpriteGetter sprites;
    private final BookModel bookModel;
    private final SpriteId bookTexture;

    public FabricEnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, SpriteId bookTexture) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookTexture = bookTexture;
    }

    public static <T extends FabricEnchantingTableLikeBlockEntity> FabricEnchantingCustomTableRenderer<T> enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new FabricEnchantingCustomTableRenderer<>(context, bookTexture(CUSTOM_TABLE_BOOK));
    }

    public static <T extends FabricEnchantingTableLikeBlockEntity> FabricEnchantingCustomTableRenderer<T> enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new FabricEnchantingCustomTableRenderer<>(context, bookTexture(CONVERSION_TABLE_BOOK));
    }

    private static SpriteId bookTexture(String path) {
        return Sheets.BLOCK_ENTITIES_MAPPER.apply(Identifier.fromNamespaceAndPath(MOD_ID, path));
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, EnchantTableRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.flip = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        renderState.open = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        renderState.time = blockEntity.time + partialTick;
        float rotationDelta = blockEntity.rot - blockEntity.oRot;

        while (rotationDelta >= (float) Math.PI) {
            rotationDelta -= (float) Math.PI * 2F;
        }
        while (rotationDelta < -(float) Math.PI) {
            rotationDelta += (float) Math.PI * 2F;
        }

        renderState.yRot = blockEntity.oRot + rotationDelta * partialTick;
    }

    @Override
    public void submit(EnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(renderState.time * 0.1F) * 0.01F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(-renderState.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float leftPageFlip = Mth.frac(renderState.flip + 0.25F) * 1.6F - 0.3F;
        float rightPageFlip = Mth.frac(renderState.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookState = BookModel.State.forAnimation(
                renderState.time,
                Mth.clamp(leftPageFlip, 0.0F, 1.0F),
                Mth.clamp(rightPageFlip, 0.0F, 1.0F),
                renderState.open
        );
        nodeCollector.submitModel(
                this.bookModel,
                bookState,
                poseStack,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.bookTexture,
                this.sprites,
                0,
                renderState.breakProgress
        );
        poseStack.popPose();
    }
}
