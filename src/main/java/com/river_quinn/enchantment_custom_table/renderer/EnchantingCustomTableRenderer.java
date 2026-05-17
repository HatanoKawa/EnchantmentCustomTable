package com.river_quinn.enchantment_custom_table.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer<T extends EnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    public static final Material BOOK_LOCATION = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchanting_table_book");
    private final MaterialSet materials;
    private final BookModel bookModel;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    @Override
    public void extractRenderState(
            T blockEntity,
            EnchantTableRenderState renderState,
            float partialTick,
            Vec3 cameraPosition,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.flip = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        renderState.open = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        renderState.time = blockEntity.time + partialTick;
        float f = blockEntity.rot - blockEntity.oRot;

        while (f >= (float)Math.PI) {
            f -= (float)(Math.PI * 2);
        }

        while (f < -(float)Math.PI) {
            f += (float)(Math.PI * 2);
        }

        renderState.yRot = blockEntity.oRot + f * partialTick;
    }

    @Override
    public void submit(EnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        poseStack.translate(0.0F, 0.1F + Mth.sin(renderState.time * 0.1F) * 0.01F, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(-renderState.yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f = Mth.frac(renderState.flip + 0.25F) * 1.6F - 0.3F;
        float f1 = Mth.frac(renderState.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookState = new BookModel.State(
                renderState.time,
                Mth.clamp(f, 0.0F, 1.0F),
                Mth.clamp(f1, 0.0F, 1.0F),
                renderState.open
        );
        nodeCollector.submitModel(
                this.bookModel,
                bookState,
                poseStack,
                BOOK_LOCATION.renderType(RenderType::entitySolid),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.materials.get(BOOK_LOCATION),
                0,
                renderState.breakProgress
        );
        poseStack.popPose();
    }
}
