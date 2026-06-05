package com.river_quinn.enchantment_custom_table.client.gui;

//? if <1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;*/
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
//?}
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
//? if <1.21.6 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
//? if >=1.21.7 {
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
//?} else {
/*import net.neoforged.neoforge.network.PacketDistributor;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EnchantmentConversionScreen extends AbstractContainerScreen<EnchantmentConversionMenu> {

    private EnchantmentConversionMenu menuContainer;
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
        //? if >=26.1 {
        super(container, inventory, text, 176, 181);
        //?} else {
        /*super(container, inventory, text);
        this.imageWidth = 176;
        this.imageHeight = 181;
        *///?}
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    //? if >=1.21.11 {
    private static final Identifier gui_bg_texture = Identifier.parse("enchantment_custom_table:textures/screens/enchantment_conversion.png");
    //?} else {
    /*private static final ResourceLocation gui_bg_texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/enchantment_conversion.png");
    *///?}

    //? if >=26.1 {
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
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
    }
    *///?}

    //? if >=1.21.9 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.minecraft.player.closeContainer();
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
    //?} else {
    /*@Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (searchBox != null && searchBox.isFocused()) {
            if (searchBox.keyPressed(key, b, c) || searchBox.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    *///?}

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
                TableMenuLayout.Conversion.PAGE_LABEL_X,
                TableMenuLayout.Conversion.PAGE_LABEL_Y,
                0xFFFFFFFF
        );

    }
    //?} else {
    /*@Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(
                this.font,
                generatePageText(),
                TableMenuLayout.Conversion.PAGE_LABEL_X,
                TableMenuLayout.Conversion.PAGE_LABEL_Y,
                -1
        );

    }
    *///?}

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
                e -> {
                    sendToServer(new EnchantmentConversionTableNetData(
                            ConversionTableIntent.PREVIOUS_PAGE
                    ));
                }
        ).bounds(this.leftPos + TableMenuLayout.Conversion.PREVIOUS_PAGE_BUTTON_X, this.topPos + TableMenuLayout.Conversion.PAGE_BUTTON_Y, TableMenuLayout.Conversion.PAGE_BUTTON_WIDTH, TableMenuLayout.Conversion.PAGE_BUTTON_HEIGHT).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    sendToServer(new EnchantmentConversionTableNetData(
                            ConversionTableIntent.NEXT_PAGE
                    ));
                }
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

        //? if >=1.21.2 {
        Registry<Enchantment> enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        //?} else {
        /*Registry<Enchantment> enchantmentRegistry = world.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        *///?}
        List<String> matchedEnchantments = new ArrayList<>();
        enchantmentRegistry.asHolderIdMap().forEach(enchantment -> {
            //? if >=1.21.11 {
            Optional<Identifier> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::identifier);
            //?} else {
            /*Optional<ResourceLocation> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::location);
            *///?}
            if (enchantmentId.isEmpty()) {
                return;
            }

            if (matchesClientSearch(query, enchantment, enchantmentId.get())) {
                matchedEnchantments.add(enchantmentId.get().toString());
            }
        });
        return matchedEnchantments.size() > EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS
                ? matchedEnchantments.subList(0, EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                : matchedEnchantments;
    }

    //? if >=1.21.11 {
    private boolean matchesClientSearch(String query, Holder<Enchantment> enchantment, Identifier enchantmentId) {
    //?} else {
    /*private boolean matchesClientSearch(String query, Holder<Enchantment> enchantment, ResourceLocation enchantmentId) {
    *///?}
        return EnchantmentSearchRules.matchesAnyCandidate(query, List.of(
                enchantmentId.toString(),
                enchantmentId.getNamespace(),
                enchantmentId.getPath(),
                enchantmentId.getPath().replace('_', ' '),
                enchantment.value().description().getString()
        ));
    }

    private void sendToServer(EnchantmentConversionTableNetData payload) {
        //? if >=1.21.7 {
        ClientPacketDistributor.sendToServer(payload);
        //?} else {
        /*PacketDistributor.sendToServer(payload);
        *///?}
    }
}
