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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer<T extends EnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "enchantment/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "enchantment/enchantment_conversion_table_book";
    private final SpriteGetter sprites;
    private final BookModel bookModel;
    private final SpriteId bookTexture;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookTexture(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, SpriteId bookTexture) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookTexture = bookTexture;
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookTexture(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookTexture(CONVERSION_TABLE_BOOK));
    }

    private static SpriteId bookTexture(String path) {
        return Sheets.BLOCK_ENTITIES_MAPPER.apply(Identifier.fromNamespaceAndPath(MOD_ID, path));
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
                this.bookTexture,
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer<T extends EnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "enchantment_conversion_table_book";
    private final MaterialSet materials;
    private final BookModel bookModel;
    private final Material bookLocation;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return Sheets.BLOCK_ENTITIES_MAPPER.apply(Identifier.fromNamespaceAndPath(MOD_ID, path));
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
                this.bookLocation.renderType(RenderTypes::entitySolid),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.materials.get(this.bookLocation),
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EnchantingCustomTableRenderer<T extends EnchantingTableLikeBlockEntity> implements BlockEntityRenderer<T, EnchantTableRenderState> {
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "enchantment_conversion_table_book";
    private final MaterialSet materials;
    private final BookModel bookModel;
    private final Material bookLocation;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.materials = context.materials();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer<EnchantingTableLikeBlockEntity> enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer<>(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return Sheets.BLOCK_ENTITIES_MAPPER.apply(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
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
                this.bookLocation.renderType(RenderType::entitySolid),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.materials.get(this.bookLocation),
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
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "entity/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "entity/enchantment_conversion_table_book";
    private final BookModel bookModel;
    private final Material bookLocation;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static EnchantingCustomTableRenderer enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
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
        VertexConsumer vertexconsumer = this.bookLocation.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
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
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "entity/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "entity/enchantment_conversion_table_book";
    private final BookModel bookModel;
    private final Material bookLocation;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static EnchantingCustomTableRenderer enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
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
        VertexConsumer vertexconsumer = this.bookLocation.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.renderToBuffer(poseStack, vertexconsumer, packedLight, packedOverlay);
        poseStack.popPose();
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
    private static final String MOD_ID = "enchantment_custom_table";
    private static final String CUSTOM_TABLE_BOOK = "entity/enchanting_custom_table_book";
    private static final String CONVERSION_TABLE_BOOK = "entity/enchantment_conversion_table_book";
    private final BookModel bookModel;
    private final Material bookLocation;

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context) {
        this(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public EnchantingCustomTableRenderer(BlockEntityRendererProvider.Context context, Material bookLocation) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.bookLocation = bookLocation;
    }

    public static EnchantingCustomTableRenderer enchantingCustom(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CUSTOM_TABLE_BOOK));
    }

    public static EnchantingCustomTableRenderer enchantmentConversion(BlockEntityRendererProvider.Context context) {
        return new EnchantingCustomTableRenderer(context, bookLocation(CONVERSION_TABLE_BOOK));
    }

    private static Material bookLocation(String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
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
        VertexConsumer vertexconsumer = this.bookLocation.buffer(bufferSource, RenderType::entitySolid);
        this.bookModel.render(poseStack, vertexconsumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(EnchantingCustomTableBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)pos.getX() + (double)1.0F, (double)pos.getY() + (double)1.5F, (double)pos.getZ() + (double)1.0F);
    }

}
*///?}
