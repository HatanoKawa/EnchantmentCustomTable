package com.river_quinn.enchantment_custom_table.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.init.ModPayloads;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnchantmentConversionScreen extends AbstractContainerScreen<EnchantmentConversionMenu> {
    private static final ResourceLocation GUI_BACKGROUND =
            new ResourceLocation("enchantment_custom_table", "textures/screens/enchantment_conversion.png");

    private final EnchantmentConversionMenu menuContainer;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_left_arrow_button;
    Button button_right_arrow_button;
    EditBox searchBox;
    private String pendingSearchQuery = "";
    private String lastSentSearchQuery = "";
    private int searchUpdateDelayTicks = -1;

    public EnchantmentConversionScreen(EnchantmentConversionMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 181;
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
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256 && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.keyPressed(key, scanCode, modifiers) || searchBox.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (searchUpdateDelayTicks > 0) {
            searchUpdateDelayTicks--;
        }
        if (searchUpdateDelayTicks == 0) {
            sendSearchRequestIfChanged();
            searchUpdateDelayTicks = -1;
        }
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
                TableMenuLayout.Conversion.PAGE_LABEL_X,
                TableMenuLayout.Conversion.PAGE_LABEL_Y,
                -1
        );
    }

    @Override
    public void init() {
        super.init();
        searchBox = new EditBox(
                this.font,
                this.leftPos + 43,
                this.topPos + 4,
                126,
                14,
                Component.translatable("gui.enchantment_custom_table.enchantment_conversion.search")
        );
        searchBox.setMaxLength(EnchantmentSearchRules.MAX_SEARCH_QUERY_LENGTH);
        searchBox.setHint(Component.translatable("gui.enchantment_custom_table.enchantment_conversion.search"));
        searchBox.setValue(pendingSearchQuery);
        searchBox.setResponder(this::queueSearchRequest);
        this.addRenderableWidget(searchBox);

        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> sendToServer(new EnchantmentConversionTableNetData(ConversionTableIntent.PREVIOUS_PAGE))
        ).bounds(this.leftPos + TableMenuLayout.Conversion.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y, TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH, TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> sendToServer(new EnchantmentConversionTableNetData(ConversionTableIntent.NEXT_PAGE))
        ).bounds(this.leftPos + TableMenuLayout.Conversion.NEXT_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y, TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH, TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_right_arrow_button);
    }

    private void queueSearchRequest(String query) {
        pendingSearchQuery = EnchantmentSearchRules.sanitizeSearchQuery(query);
        searchUpdateDelayTicks = 6;
    }

    private void sendSearchRequestIfChanged() {
        if (Objects.equals(pendingSearchQuery, lastSentSearchQuery)) {
            return;
        }

        lastSentSearchQuery = pendingSearchQuery;
        String clientLanguage = getClientLanguage();
        List<String> matchedEnchantments = findClientLocalizedMatches(pendingSearchQuery);
        menuContainer.setSearchQuery(pendingSearchQuery, clientLanguage, matchedEnchantments);
        sendToServer(new EnchantmentConversionTableNetData(
                ConversionTableIntent.SEARCH,
                pendingSearchQuery,
                clientLanguage,
                matchedEnchantments
        ));
    }

    private String getClientLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return "";
        }
        return EnchantmentSearchRules.sanitizeClientLanguage(minecraft.getLanguageManager().getSelected());
    }

    private List<String> findClientLocalizedMatches(String query) {
        if (EnchantmentSearchRules.isBlankSearch(query)) {
            return List.of();
        }

        List<String> matchedEnchantments = new ArrayList<>();
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            ResourceLocation enchantmentId = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            if (enchantmentId == null) {
                continue;
            }
            if (matchesClientSearch(query, enchantment, enchantmentId)) {
                matchedEnchantments.add(enchantmentId.toString());
            }
            if (matchedEnchantments.size() >= EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS) {
                break;
            }
        }
        return matchedEnchantments;
    }

    private boolean matchesClientSearch(String query, Enchantment enchantment, ResourceLocation enchantmentId) {
        return EnchantmentSearchRules.matchesAnyCandidate(query, List.of(
                enchantmentId.toString(),
                enchantmentId.getNamespace(),
                enchantmentId.getPath(),
                enchantmentId.getPath().replace('_', ' '),
                EnchantmentUtils.getCoreEnchantmentKey(enchantment).asString(),
                enchantment.getFullname(1).getString()
        ));
    }

    private void sendToServer(EnchantmentConversionTableNetData payload) {
        ModPayloads.sendToServer(payload);
    }
}
