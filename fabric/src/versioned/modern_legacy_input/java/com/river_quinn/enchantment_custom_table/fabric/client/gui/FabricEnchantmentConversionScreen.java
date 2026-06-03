package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.river_quinn.enchantment_custom_table.fabric.network.FabricConversionSearchPayload;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabricEnchantmentConversionScreen extends AbstractContainerScreen<FabricEnchantmentConversionMenu> {
    private static final ResourceLocation GUI_BACKGROUND = FabricVersionedMinecraft.id("textures/screens/enchantment_conversion.png");
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
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
        searchBox.setHint(Component.translatable("gui.enchantment_custom_table.enchantment_conversion.search"));
        searchBox.setValue(pendingSearchQuery);
        searchBox.setResponder(this::queueSearchRequest);
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }).bounds(this.leftPos + 7, this.topPos + 4, 17, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(this.leftPos + 24, this.topPos + 4, 17, 18).build());
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
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, generatePageText(), 24, 66, -1);
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
        ClientPlayNetworking.send(new FabricConversionSearchPayload(pendingSearchQuery, clientLanguage, matchedEnchantments));
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
        Registry<Enchantment> enchantmentRegistry = FabricVersionedMinecraft.enchantmentRegistry(menu.world);
        List<String> matchedEnchantments = new ArrayList<>();
        enchantmentRegistry.asHolderIdMap().forEach(enchantment -> {
            FabricEnchantmentUtils.getEnchantmentKey(menu.world, enchantment).ifPresent(key -> {
                List<String> candidates = List.of(
                        FabricVersionedMinecraft.keyId(key),
                        FabricVersionedMinecraft.keyNamespace(key),
                        FabricVersionedMinecraft.keyPath(key),
                        FabricVersionedMinecraft.keyPath(key).replace('_', ' '),
                        enchantment.value().description().getString()
                );
                if (EnchantmentSearchRules.matchesAnyCandidate(query, candidates)) {
                    matchedEnchantments.add(FabricVersionedMinecraft.keyId(key));
                }
            });
        });
        return matchedEnchantments.stream()
                .limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                .toList();
    }
}
