package com.river_quinn.enchantment_custom_table.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import com.river_quinn.enchantment_custom_table.init.ModPayloads;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EnchantingCustomScreen extends AbstractContainerScreen<EnchantingCustomMenu> {
    private static final ResourceLocation GUI_BACKGROUND =
            new ResourceLocation("enchantment_custom_table", "textures/screens/enchanting_custom.png");
    private static final ResourceLocation ARROW_TEXTURE =
            new ResourceLocation("enchantment_custom_table", "textures/screens/left_arrow.png");

    private final EnchantingCustomMenu menuContainer;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    Button export_button;

    public EnchantingCustomScreen(EnchantingCustomMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        guiGraphics.blit(ARROW_TEXTURE, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    public String generatePageText() {
        int currentPage = this.menuContainer.currentPage;
        int totalPage = this.menuContainer.totalPage;
        if (totalPage == 0) {
            return "-/-";
        }
        return (currentPage + 1) + "/" + totalPage;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(
                this.font,
                generatePageText(),
                TableMenuLayout.Enchanting.PAGE_LABEL_X,
                TableMenuLayout.Enchanting.PAGE_LABEL_Y,
                -1
        );
    }

    @Override
    public void init() {
        super.init();
        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> sendToServer(new EnchantingCustomTableNetData(EnchantingTableIntent.PREVIOUS_PAGE))
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> sendToServer(new EnchantingCustomTableNetData(EnchantingTableIntent.NEXT_PAGE))
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.NEXT_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_right_arrow_button);

        export_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                e -> sendToServer(new EnchantingCustomTableNetData(EnchantingTableIntent.EXPORT_ALL_ENCHANTMENTS))
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_Y, TableMenuLayout.Enchanting.EXPORT_BUTTON_WIDTH, TableMenuLayout.Enchanting.EXPORT_BUTTON_HEIGHT).build();
        this.addRenderableWidget(export_button);
    }

    private void sendToServer(EnchantingCustomTableNetData payload) {
        ModPayloads.sendToServer(payload);
    }
}
