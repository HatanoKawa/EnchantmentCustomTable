package com.river_quinn.enchantment_custom_table.renderer;

//? if >=26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
import javax.annotation.Nullable;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer<T extends EnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    public static final SpriteId BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchantment/enchanting_table_book");
    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
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
        BookModel.State bookState = BookModel.State.forAnimation(
                renderState.time,
                Mth.clamp(f, 0.0F, 1.0F),
                Mth.clamp(f1, 0.0F, 1.0F),
                renderState.open
        );
        nodeCollector.submitModel(
                this.bookModel,
                bookState,
                poseStack,
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                BOOK_TEXTURE,
                this.sprites,
                0,
                renderState.breakProgress
        );
        poseStack.popPose();
    }
}
//?} else if >=1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
                BOOK_LOCATION.renderType(RenderTypes::entitySolid),
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
*///?} else if >=1.21.9 {
/*import com.mojang.blaze3d.vertex.PoseStack;
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
*///?} else if >=1.21.5 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
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
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer implements BlockEntityRenderer<EnchantingTableLikeBlockEntity> {
    public static final Material BOOK_LOCATION;
    private final BookModel bookModel;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    public void render(EnchantingTableLikeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, Vec3 vec3) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float f = (float)blockEntity.time + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0F);

        float f1;
        for(f1 = blockEntity.rot - blockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
        }

        while(f1 < -(float)Math.PI) {
            f1 += ((float)Math.PI * 2F);
        }

        float f2 = blockEntity.oRot + f1 * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-f2));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    static {
        BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
    }
}
*///?} else if >=1.21.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
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

public class EnchantingCustomTableRenderer implements BlockEntityRenderer<EnchantingTableLikeBlockEntity> {
    public static final Material BOOK_LOCATION;
    private final BookModel bookModel;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    public void render(EnchantingTableLikeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float f = (float)blockEntity.time + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0F);

        float f1;
        for(f1 = blockEntity.rot - blockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
        }

        while(f1 < -(float)Math.PI) {
            f1 += ((float)Math.PI * 2F);
        }

        float f2 = blockEntity.oRot + f1 * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-f2));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    static {
        BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
    }
}
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingTableLikeBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class EnchantingCustomTableRenderer implements BlockEntityRenderer<EnchantingTableLikeBlockEntity> {
    public static final Material BOOK_LOCATION;
    private final BookModel bookModel;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    public void render(EnchantingTableLikeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float f = (float)blockEntity.time + partialTick;
        poseStack.translate(0.0F, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0F);

        float f1;
        for(f1 = blockEntity.rot - blockEntity.oRot; f1 >= (float)Math.PI; f1 -= ((float)Math.PI * 2F)) {
        }

        while(f1 < -(float)Math.PI) {
            f1 += ((float)Math.PI * 2F);
        }

        float f2 = blockEntity.oRot + f1 * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-f2));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(partialTick, blockEntity.oFlip, blockEntity.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(partialTick, blockEntity.oOpen, blockEntity.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(EnchantingCustomTableBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)pos.getX() + (double)1.0F, (double)pos.getY() + (double)1.5F, (double)pos.getZ() + (double)1.0F);
    }

    static {
        BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));
    }
}
*///?}
