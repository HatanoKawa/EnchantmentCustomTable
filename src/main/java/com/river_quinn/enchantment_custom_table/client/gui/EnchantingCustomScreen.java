package com.river_quinn.enchantment_custom_table.client.gui;

import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class EnchantingCustomScreen extends AbstractContainerScreen<EnchantingCustomMenu> {

    private EnchantingCustomMenu menuContainer;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    Button export_button;

    public EnchantingCustomScreen(EnchantingCustomMenu container, Inventory inventory, Component text) {
        super(container, inventory, text, 176, 166);
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    private static final Identifier gui_bg_texture = Identifier.parse("enchantment_custom_table:textures/screens/enchanting_custom.png");
    private static final Identifier arrow_texture = Identifier.parse("enchantment_custom_table:textures/screens/left_arrow.png");

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.blit(RenderPipelines.GUI_TEXTURED, arrow_texture, this.leftPos + 27, this.topPos + 12, 0, 0, 12, 9, 12, 9);

    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(event);
    }

    public String generatePageText() {
        int currentPage = this.menuContainer.currentPage;
        int totalPage = this.menuContainer.totalPage;
        if (totalPage == 0)
            return "-/-";
        return (currentPage + 1) + "/" + totalPage;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(
                this.font,
                generatePageText(),
                35,
                33,
                0xFFFFFFFF
        );

    }

    @Override
    public void init() {
        super.init();
        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> {
                    ClientPacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.PREVIOUS_PAGE
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 43, 26, 18).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    ClientPacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.NEXT_PAGE
                    ));
                }
        ).bounds(this.leftPos + 33, this.topPos + 43, 26, 18).build();
        this.addRenderableWidget(button_right_arrow_button);

        export_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                e -> {
                    ClientPacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.EXPORT_ALL_ENCHANTMENTS
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 61, 52, 18).build();
        this.addRenderableWidget(export_button);
    }

}
