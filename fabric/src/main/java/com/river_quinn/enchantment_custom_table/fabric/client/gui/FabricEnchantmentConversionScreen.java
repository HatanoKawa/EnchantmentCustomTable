package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.fabric.network.FabricConversionSearchPayload;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabricEnchantmentConversionScreen extends AbstractContainerScreen<FabricEnchantmentConversionMenu> {
    private static final ResourceLocation GUI_BACKGROUND = FabricVersionedMinecraft.id("textures/gui/container/enchantment_conversion.png");
    private EditBox searchBox;
    private String pendingSearchQuery = "";
    private String lastSentSearchQuery = "";
    private int searchUpdateDelayTicks = -1;

    public FabricEnchantmentConversionScreen(FabricEnchantmentConversionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 181;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void init() {
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
        searchBox.setSuggestion(Component.translatable("gui.enchantment_custom_table.enchantment_conversion.search").getString());
        searchBox.setValue(pendingSearchQuery);
        searchBox.setResponder(this::queueSearchRequest);
        addRenderableWidget(searchBox);

        addRenderableWidget(new Button(
                this.leftPos + TableMenuLayout.Conversion.PREVIOUS_PAGE_BUTTON_X,
                this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y,
                TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH,
                TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT,
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                button -> clickMenuButton(0)
        ));
        addRenderableWidget(new Button(
                this.leftPos + TableMenuLayout.Conversion.NEXT_PAGE_BUTTON_X,
                this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y,
                TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH,
                TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT,
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                button -> clickMenuButton(1)
        ));
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

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == 256) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
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
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, GUI_BACKGROUND);
        blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        drawCenteredString(poseStack, this.font, generatePageText(), TableMenuLayout.Conversion.PAGE_LABEL_X, TableMenuLayout.Conversion.PAGE_LABEL_Y, -1);
    }

    private String generatePageText() {
        int totalPage = menu.totalPage();
        if (totalPage == 0) {
            return "-/-";
        }
        return (menu.currentPage() + 1) + "/" + totalPage;
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
        menu.setSearchQuery(pendingSearchQuery, clientLanguage, matchedEnchantments);
        FabricConversionSearchPayload payload = new FabricConversionSearchPayload(pendingSearchQuery, clientLanguage, matchedEnchantments);
        FriendlyByteBuf buffer = PacketByteBufs.create();
        payload.write(buffer);
        ClientPlayNetworking.send(FabricConversionSearchPayload.ID, buffer);
    }

    private String getClientLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return "";
        }
        return EnchantmentSearchRules.sanitizeClientLanguage(minecraft.getLanguageManager().getSelected().getCode());
    }

    private List<String> findClientLocalizedMatches(String query) {
        if (EnchantmentSearchRules.isBlankSearch(query)) {
            return List.of();
        }
        List<String> matchedEnchantments = new ArrayList<>();
        for (Enchantment enchantment : FabricEnchantmentUtils.allEnchantments()) {
            var key = FabricEnchantmentUtils.getCoreEnchantmentKey(enchantment);
            List<String> candidates = List.of(
                    key.asString(),
                    key.namespace(),
                    key.path(),
                    key.path().replace('_', ' '),
                    enchantment.getFullname(1).getString()
            );
            if (EnchantmentSearchRules.matchesAnyCandidate(query, candidates)) {
                matchedEnchantments.add(key.asString());
            }
        }
        return matchedEnchantments.stream()
                .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                .toList();
    }

    private void clickMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }
}
