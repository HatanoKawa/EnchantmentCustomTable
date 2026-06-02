package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FabricEnchantingCustomScreen extends AbstractContainerScreen<FabricEnchantingCustomMenu> {
    private static final ResourceLocation GUI_BACKGROUND = EnchantmentCustomTableFabric.id("textures/screens/enchanting_custom.png");
    private static final ResourceLocation ARROW = EnchantmentCustomTableFabric.id("textures/screens/left_arrow.png");

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
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.blit(ARROW, this.leftPos + 27, this.topPos + 12, 0, 0, 12, 9, 12, 9);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, "-/-", 35, 33, -1);
    }
}
