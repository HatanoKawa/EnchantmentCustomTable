package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FabricEnchantingCustomScreen extends AbstractContainerScreen<FabricEnchantingCustomMenu> {
    private static final ResourceLocation GUI_BACKGROUND = FabricVersionedMinecraft.id("textures/gui/container/enchanting_custom.png");
    private static final ResourceLocation ARROW = FabricVersionedMinecraft.id("textures/gui/container/left_arrow.png");

    public FabricEnchantingCustomScreen(FabricEnchantingCustomMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new Button(
                this.leftPos + TableMenuLayout.Enchanting.PREVIOUS_PAGE_BUTTON_X,
                this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y,
                TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH,
                TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT,
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                button -> clickMenuButton(0)
        ));
        addRenderableWidget(new Button(
                this.leftPos + TableMenuLayout.Enchanting.NEXT_PAGE_BUTTON_X,
                this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y,
                TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH,
                TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT,
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                button -> clickMenuButton(1)
        ));
        addRenderableWidget(new Button(
                this.leftPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_X,
                this.topPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_Y,
                TableMenuLayout.Enchanting.EXPORT_BUTTON_WIDTH,
                TableMenuLayout.Enchanting.EXPORT_BUTTON_HEIGHT,
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                button -> clickMenuButton(2)
        ));
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, GUI_BACKGROUND);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.setShaderTexture(0, ARROW);
        blit(poseStack, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        drawCenteredString(poseStack, this.font, generatePageText(), TableMenuLayout.Enchanting.PAGE_LABEL_X, TableMenuLayout.Enchanting.PAGE_LABEL_Y, -1);
    }

    private String generatePageText() {
        int totalPage = menu.totalPage();
        if (totalPage == 0) {
            return "-/-";
        }
        return (menu.currentPage() + 1) + "/" + totalPage;
    }

    private void clickMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }
}
