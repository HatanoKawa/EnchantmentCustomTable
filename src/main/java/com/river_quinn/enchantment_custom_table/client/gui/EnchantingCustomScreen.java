package com.river_quinn.enchantment_custom_table.client.gui;

import com.mojang.logging.LogUtils;
import com.river_quinn.enchantment_custom_table.network.enchanting_custom_table.EnchantingCustomTableNetData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentCustomMenu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

public class EnchantingCustomScreen extends AbstractContainerScreen<EnchantmentCustomMenu> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private EnchantmentCustomMenu menuContainer;
    private final static HashMap<String, Object> guistate = EnchantmentCustomMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
//    Button button_left_arrow;
//    Button button_right_arrow;
    // ImageButton imagebutton_enchanting_table_tab_selected;
    // ImageButton imagebutton_enchanting_table_tab_unselected;
    // ImageButton imagebutton_enchanted_book;
    // ImageButton imagebutton_enchanting_custom_table_top;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    Button export_button;
//    ImageButton imagebutton_burn_progress;

    public EnchantingCustomScreen(EnchantmentCustomMenu container, Inventory inventory, Component text) {
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

    private static final ResourceLocation texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/enchantment_custom.png");

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
        int currentPage = this.menuContainer.boundBlockEntity.currentPage;
        int totalPage = this.menuContainer.boundBlockEntity.totalPage;
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
                e -> menuContainer.boundBlockEntity.previousPage()
        ).bounds(this.leftPos + 7, this.topPos + 43, 26, 18).build();
        guistate.put("button:button_left_arrow_button", button_left_arrow_button);
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                        menuContainer.boundBlockEntity.nextPage();
                }
        ).bounds(this.leftPos + 33, this.topPos + 43, 26, 18).build();
        guistate.put("button:button_right_arrow_button", button_right_arrow_button);
        this.addRenderableWidget(button_right_arrow_button);

        export_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_export"),
                e -> PacketDistributor.sendToServer(new EnchantingCustomTableNetData(
                        x, y, z,
                        EnchantingCustomTableNetData.OperateType.EXPORT_ALL_ENCHANTMENTS.toString()
                ))
        ).bounds(this.leftPos + 7, this.topPos + 61, 52, 18).build();
        guistate.put("button:export_button", export_button);
        this.addRenderableWidget(export_button);


//        button_left_arrow = Button.builder(Component.translatable("gui.enchantmentcustomtable.enchantment_custom.button_left_arrow"), e -> {
//        }).bounds(this.leftPos + 42, this.topPos + 61, 30, 20).build();
//        guistate.put("button:button_left_arrow", button_left_arrow);
//        this.addRenderableWidget(button_left_arrow);
//        button_right_arrow = Button.builder(Component.translatable("gui.enchantmentcustomtable.enchantment_custom.button_right_arrow"), e -> {
//        }).bounds(this.leftPos + 138, this.topPos + 61, 30, 20).build();
//        guistate.put("button:button_right_arrow", button_right_arrow);
//        this.addRenderableWidget(button_right_arrow);
        // imagebutton_enchanting_table_tab_selected = new ImageButton(this.leftPos + -28, this.topPos + 9, 32, 26,
        //         new WidgetSprites(ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_table_tab_selected.png"), ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_table_tab_selected.png")), e -> {
        // }) {
        //     @Override
        //     public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        //         guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
        //     }
        // };
        // guistate.put("button:imagebutton_enchanting_table_tab_selected", imagebutton_enchanting_table_tab_selected);
        // this.addRenderableWidget(imagebutton_enchanting_table_tab_selected);
        // imagebutton_enchanting_table_tab_unselected = new ImageButton(this.leftPos + -32, this.topPos + 39, 32, 26,
        //         new WidgetSprites(ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_table_tab_unselected.png"), ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_table_tab_unselected.png")), e -> {
        // }) {
        //     @Override
        //     public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        //         guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
        //     }
        // };
        // guistate.put("button:imagebutton_enchanting_table_tab_unselected", imagebutton_enchanting_table_tab_unselected);
        // this.addRenderableWidget(imagebutton_enchanting_table_tab_unselected);
        // imagebutton_enchanted_book = new ImageButton(this.leftPos + -20, this.topPos + 44, 16, 16,
        //         new WidgetSprites(ResourceLocation.parse("minecraft:textures/item/enchanted_book.png"), ResourceLocation.parse("minecraft:textures/item/enchanted_book.png")), e -> {
        // }) {
        //     @Override
        //     public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        //         guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
        //     }
        // };
        // guistate.put("button:imagebutton_enchanted_book", imagebutton_enchanted_book);
        // this.addRenderableWidget(imagebutton_enchanted_book);
        // imagebutton_enchanting_custom_table_top = new ImageButton(this.leftPos + -20, this.topPos + 14, 16, 16,
        //         new WidgetSprites(ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_custom_table_top.png"), ResourceLocation.parse("enchantmentcustomtable:textures/screens/enchanting_custom_table_top.png")), e -> {
        // }) {
        //     @Override
        //     public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        //         guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
        //     }
        // };
        // guistate.put("button:imagebutton_enchanting_custom_table_top", imagebutton_enchanting_custom_table_top);
        // this.addRenderableWidget(imagebutton_enchanting_custom_table_top);
//         imagebutton_burn_progress = new ImageButton(this.leftPos + 16, this.topPos + 31, 16, 24,
//                 new WidgetSprites(ResourceLocation.parse("enchantment_custom_table:textures/screens/burn_progress.png"), ResourceLocation.parse("enchantment_custom_table:textures/screens/burn_progress.png")), e -> {
//         }) {
//             @Override
//             public void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
//                 guiGraphics.blit(sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
//             }
//         };
//         guistate.put("button:imagebutton_burn_progress", imagebutton_burn_progress);
//         this.addRenderableWidget(imagebutton_burn_progress);
    }

    public void ShowEnchantmentCustomPage() {

    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
//        LOGGER.info("Mouse released {} {} {}", mouseX, mouseY, button);
        // check if click button_left_arrow
//        if (button_left_arrow.isMouseOver(mouseX, mouseY)) {
//            LOGGER.info("Button button_left_arrow clicked");
//            this.menu.turnPreviousPage();
//            return true;
//        }
//        // check if click button_right_arrow
//        if (button_right_arrow.isMouseOver(mouseX, mouseY)) {
//            LOGGER.info("Button button_right_arrow clicked");
//            this.menu.turnNextPage();
//            return true;
//        }
        return true;
    }
}
