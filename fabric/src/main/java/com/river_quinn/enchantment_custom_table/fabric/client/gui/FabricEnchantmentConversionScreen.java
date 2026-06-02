package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.river_quinn.enchantment_custom_table.fabric.EnchantmentCustomTableFabric;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FabricEnchantmentConversionScreen extends AbstractContainerScreen<FabricEnchantmentConversionMenu> {
    private static final ResourceLocation GUI_BACKGROUND = EnchantmentCustomTableFabric.id("textures/screens/enchantment_conversion.png");

    public FabricEnchantmentConversionScreen(FabricEnchantmentConversionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 181;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, "-/-", 24, 66, -1);
    }
}
