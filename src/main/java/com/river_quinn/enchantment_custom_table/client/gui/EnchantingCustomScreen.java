package com.river_quinn.enchantment_custom_table.client.gui;

import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.network.PacketDistributor;

public class EnchantingCustomScreen extends AbstractContainerScreen<EnchantingCustomMenu> {

    private EnchantingCustomMenu menuContainer;
    private final static HashMap<String, Object> guistate = EnchantingCustomMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    Button export_button;

    public EnchantingCustomScreen(EnchantingCustomMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private static final ResourceLocation texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/enchanting_custom.png");

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        guiGraphics.blit(ResourceLocation.parse("enchantment_custom_table:textures/screens/left_arrow.png"), this.leftPos + 27, this.topPos + 12, 0, 0, 12, 9, 12, 9);

    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    public String generatePageText() {
        int currentPage = this.menuContainer.currentPage;
        int totalPage = this.menuContainer.totalPage;
        if (totalPage == 0)
            return "-/-";
        return (currentPage + 1) + "/" + totalPage;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(
                this.font,
                generatePageText(),
                35,
                33,
                -1
        );

    }

    @Override
    public void init() {
        super.init();
        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> {
                    menuContainer.previousPage();
                    PacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.PREVIOUS_PAGE.name()
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 43, 26, 18).build();
        guistate.put("button:button_left_arrow_button", button_left_arrow_button);
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    menuContainer.nextPage();
                    PacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.NEXT_PAGE.name()
                    ));
                }
        ).bounds(this.leftPos + 33, this.topPos + 43, 26, 18).build();
        guistate.put("button:button_right_arrow_button", button_right_arrow_button);
        this.addRenderableWidget(button_right_arrow_button);

        export_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                e -> {
                    menuContainer.exportAllEnchantments();
                    PacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingCustomTableNetData.OperateType.EXPORT_ALL_ENCHANTMENTS.name()
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 61, 52, 18).build();
        guistate.put("button:export_button", export_button);
        this.addRenderableWidget(export_button);
    }

}
