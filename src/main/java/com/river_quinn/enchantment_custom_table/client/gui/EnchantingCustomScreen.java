package com.river_quinn.enchantment_custom_table.client.gui;

//? if <1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import com.river_quinn.enchantment_custom_table.core.net.EnchantingTableIntent;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
//?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;*/
//?}
import net.minecraft.client.gui.components.Button;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
//? if <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

//? if >=1.21.7 {
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
//?} else {
/*import net.neoforged.neoforge.network.PacketDistributor;
*///?}

public class EnchantingCustomScreen extends AbstractContainerScreen<EnchantingCustomMenu> {

    private EnchantingCustomMenu menuContainer;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    Button export_button;

    public EnchantingCustomScreen(EnchantingCustomMenu container, Inventory inventory, Component text) {
        //? if >=26.1 {
        super(container, inventory, text, 176, 166);
        //?} else {
        /*super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 166;
        *///?}
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    //? if >=1.21.11 {
    private static final Identifier gui_bg_texture = Identifier.parse("enchantment_custom_table:textures/screens/enchanting_custom.png");
    private static final Identifier arrow_texture = Identifier.parse("enchantment_custom_table:textures/screens/left_arrow.png");
    //?} else {
    /*private static final ResourceLocation gui_bg_texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/enchanting_custom.png");
    private static final ResourceLocation arrow_texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/left_arrow.png");
    *///?}

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        graphics.blit(RenderPipelines.GUI_TEXTURED, arrow_texture, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);

    }
    //?} else if >=1.21.6 {
    /*@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, arrow_texture, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);

    }
    *///?} else if >=1.21.2 {
    /*@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        guiGraphics.blit(RenderType::guiTextured, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        guiGraphics.blit(RenderType::guiTextured, arrow_texture, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);

    }
    *///?} else {
    /*@Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        guiGraphics.blit(arrow_texture, this.leftPos + TableMenuLayout.Enchanting.ARROW_X, this.topPos + TableMenuLayout.Enchanting.ARROW_Y, 0, 0, 12, 9, 12, 9);

    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(event);
    }
    //?} else {
    /*@Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }
    *///?}

    public String generatePageText() {
        int currentPage = this.menuContainer.currentPage;
        int totalPage = this.menuContainer.totalPage;
        if (totalPage == 0)
            return "-/-";
        return (currentPage + 1) + "/" + totalPage;
    }

    //? if >=26.1 {
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(
                this.font,
                generatePageText(),
                TableMenuLayout.Enchanting.PAGE_LABEL_X,
                TableMenuLayout.Enchanting.PAGE_LABEL_Y,
                0xFFFFFFFF
        );

    }
    //?} else {
    /*@Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(
                this.font,
                generatePageText(),
                TableMenuLayout.Enchanting.PAGE_LABEL_X,
                TableMenuLayout.Enchanting.PAGE_LABEL_Y,
                -1
        );

    }
    *///?}

    @Override
    public void init() {
        super.init();
        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> {
                    sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.PREVIOUS_PAGE
                    ));
                }
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.NEXT_PAGE
                    ));
                }
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.NEXT_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.PAGE_BUTTON_Y, TableMenuLayout.Enchanting.PAGE_BUTTON_WIDTH, TableMenuLayout.Enchanting.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_right_arrow_button);

        export_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                e -> {
                    sendToServer(new EnchantingCustomTableNetData(
                            EnchantingTableIntent.EXPORT_ALL_ENCHANTMENTS
                    ));
                }
        ).bounds(this.leftPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_X, this.topPos + TableMenuLayout.Enchanting.EXPORT_BUTTON_Y, TableMenuLayout.Enchanting.EXPORT_BUTTON_WIDTH, TableMenuLayout.Enchanting.EXPORT_BUTTON_HEIGHT).build();
        this.addRenderableWidget(export_button);
    }

    private void sendToServer(EnchantingCustomTableNetData payload) {
        //? if >=1.21.7 {
        ClientPacketDistributor.sendToServer(payload);
        //?} else {
        /*PacketDistributor.sendToServer(payload);
        *///?}
    }

}
