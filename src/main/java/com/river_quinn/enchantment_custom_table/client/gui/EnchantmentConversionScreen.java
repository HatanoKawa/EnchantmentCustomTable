package com.river_quinn.enchantment_custom_table.client.gui;

import com.river_quinn.enchantment_custom_table.core.net.ConversionTableIntent;
import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

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
        super(container, inventory, text);
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 181;
    }

    private static final ResourceLocation gui_bg_texture = ResourceLocation.parse("enchantment_custom_table:textures/screens/enchantment_conversion.png");

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
//        RenderSystem.setShaderColor(1, 1, 1, 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
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

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawCenteredString(
                this.font,
                generatePageText(),
                24,
                9,
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
                e -> {
                    PacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                            ConversionTableIntent.PREVIOUS_PAGE
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 4, 17, 18).build();
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    PacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                            ConversionTableIntent.NEXT_PAGE
                    ));
                }
        ).bounds(this.leftPos + 24, this.topPos + 4, 17, 18).build();
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
        PacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                ConversionTableIntent.SEARCH,
                pendingSearchQuery,
                getClientLanguage(),
                findClientLocalizedMatches(pendingSearchQuery)
        ));
    }

    private String getClientLanguage() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return "";
        }
        return EnchantmentSearchRules.sanitizeClientLanguage(minecraft.getLanguageManager().getSelected());
    }

    private List<ResourceLocation> findClientLocalizedMatches(String query) {
        if (EnchantmentSearchRules.isBlankSearch(query)) {
            return List.of();
        }

        Registry<Enchantment> enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<ResourceLocation> matchedEnchantments = new ArrayList<>();
        enchantmentRegistry.asHolderIdMap().forEach(enchantment -> {
            Optional<ResourceLocation> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::location);
            if (enchantmentId.isEmpty()) {
                return;
            }

            if (matchesClientSearch(query, enchantment, enchantmentId.get())) {
                matchedEnchantments.add(enchantmentId.get());
            }
        });
        return matchedEnchantments.size() > EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS
                ? matchedEnchantments.subList(0, EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
                : matchedEnchantments;
    }

    private boolean matchesClientSearch(String query, Holder<Enchantment> enchantment, ResourceLocation enchantmentId) {
        return EnchantmentSearchRules.matchesAnyCandidate(query, List.of(
                enchantmentId.toString(),
                enchantmentId.getNamespace(),
                enchantmentId.getPath(),
                enchantmentId.getPath().replace('_', ' '),
                enchantment.value().description().getString()
        ));
    }
}
