package com.river_quinn.enchantment_custom_table.fabric.client.gui;

import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.fabric.network.FabricConversionSearchPayload;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricVersionedMinecraft;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FabricEnchantmentConversionScreen extends AbstractContainerScreen<FabricEnchantmentConversionMenu> {
    private static final Identifier GUI_BACKGROUND = FabricVersionedMinecraft.id("textures/screens/enchantment_conversion.png");
    private EditBox searchBox;
    private String pendingSearchQuery = "";
    private String lastSentSearchQuery = "";
    private int searchUpdateDelayTicks = -1;

    public FabricEnchantmentConversionScreen(FabricEnchantmentConversionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 181);
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
        }).bounds(this.leftPos + TableMenuLayout.Conversion.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y, TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH, TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(this.leftPos + TableMenuLayout.Conversion.NEXT_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y, TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH, TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT).build());
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
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.keyPressed(event) || searchBox.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchBox != null && searchBox.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(this.font, generatePageText(), TableMenuLayout.Conversion.PAGE_LABEL_X, TableMenuLayout.Conversion.PAGE_LABEL_Y, 0xFFFFFFFF);
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
