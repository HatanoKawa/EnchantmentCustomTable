package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }).bounds(this.leftPos + TableMenuLayout.Enchanting.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(this.leftPos + TableMenuLayout.Enchanting.NEXT_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            }
        }).bounds(this.leftPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_Y, TableMenuLayout.Enchanting.EXPORT_BUTTON_WIDTH, TableMenuLayout.Enchanting.EXPORT_BUTTON_HEIGHT).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.blit(ARROW, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, generatePageText(), TableMenuLayout.Enchanting.PAGE_LABEL_X, TableMenuLayout.Enchanting.PAGE_LABEL_Y, -1);
    }

    private String generatePageText() {
        int totalPage = menu.totalPage();
        if (totalPage == 0) {
            return "-/-";
        }
        return (menu.currentPage() + 1) + "/" + totalPage;
    }
}
