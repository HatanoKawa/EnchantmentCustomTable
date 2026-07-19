package com.river_quinn.enchantment_custom_table.fabric.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingTableLikeBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FabricEnchantingCustomTableRenderer<T extends FabricEnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T> {
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "entity/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "entity/enchantment_conversion_table_book";

    private final BookModel bookModel;
    private final Material bookLocation;

    public FabricEnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static <T extends FabricEnchantingTableLikeBlockEntity> FabricEnchantingCustomTableRenderer<T> enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new FabricEnchantingCustomTableRenderer<>(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static <T extends FabricEnchantingTableLikeBlockEntity> FabricEnchantingCustomTableRenderer<T> enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new FabricEnchantingCustomTableRenderer<>(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(MOD_ID, path));
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float time = blockEntity.time + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);

        float rotationDelta;
        for (rotationDelta = blockEntity.rot - blockEntity.oRot; rotationDelta >= (float) Math.PI; rotationDelta -= (float) Math.PI * 2F) {
        }
        while (rotationDelta < -(float) Math.PI) {
            rotationDelta += (float) Math.PI * 2F;
        }

        float rotation = blockEntity.oRot + rotationDelta * partialTick;
        poseStack.mulPose(Vector3f.YP.rotation(-rotation));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(80.0F));
        float flip = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        float leftPageFlip = Mth.frac(flip + 0.25F) * 1.6F - 0.3F;
        float rightPageFlip = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
        float open = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        this.bookModel.setupAnim(time, Mth.clamp(leftPageFlip, 0.0F, 1.0F), Mth.clamp(rightPageFlip, 0.0F, 1.0F), open);
        VertexConsumer vertexConsumer = this.bookLocation.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

}
