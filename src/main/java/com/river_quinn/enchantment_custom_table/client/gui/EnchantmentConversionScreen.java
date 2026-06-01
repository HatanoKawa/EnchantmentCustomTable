package com.river_quinn.enchantment_custom_table.client.gui;

import com.river_quinn.enchantment_custom_table.network.enchanted_book_converting_table.EnchantmentConversionTableNetData;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EnchantmentConversionScreen extends AbstractContainerScreen<EnchantmentConversionMenu> {

    private EnchantmentConversionMenu menuContainer;
    private final static HashMap<String, Object> guistate = EnchantmentConversionMenu.guistate;
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
        super(container, inventory, text, 176, 181);
        this.menuContainer = container;
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
    }

    private static final Identifier gui_bg_texture = Identifier.parse("enchantment_custom_table:textures/screens/enchantment_conversion.png");

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui_bg_texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

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
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(
                this.font,
                generatePageText(),
                24,
                51 + 15,
                0xFFFFFFFF
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
        guistate.put("text:search_box", searchBox);
        this.addRenderableWidget(searchBox);

        button_left_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_left_arrow"),
                e -> {
                    menuContainer.previousPage();
                    ClientPacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                            EnchantmentConversionTableNetData.OperateType.PREVIOUS_PAGE.name()
                    ));
                }
        ).bounds(this.leftPos + 7, this.topPos + 4, 17, 18).build();
        guistate.put("button:button_left_arrow_button", button_left_arrow_button);
        this.addRenderableWidget(button_left_arrow_button);

        button_right_arrow_button = Button.builder(
                Component.translatable("gui.enchantment_custom_table.enchantment_custom.button_right_arrow"),
                e -> {
                    menuContainer.nextPage();
                    ClientPacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                            EnchantmentConversionTableNetData.OperateType.NEXT_PAGE.name()
                    ));
                }
        ).bounds(this.leftPos + 24, this.topPos + 4, 17, 18).build();
        guistate.put("button:button_right_arrow_button", button_right_arrow_button);
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
        List<Identifier> matchedEnchantments = findClientLocalizedMatches(pendingSearchQuery);
        menuContainer.setSearchQuery(pendingSearchQuery, clientLanguage, matchedEnchantments);
        ClientPacketDistributor.sendToServer(new EnchantmentConversionTableNetData(
                EnchantmentConversionTableNetData.OperateType.SEARCH.name(),
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

    private List<Identifier> findClientLocalizedMatches(String query) {
        if (EnchantmentSearchRules.isBlankSearch(query)) {
            return List.of();
        }

        Registry<Enchantment> enchantmentRegistry = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        List<Identifier> matchedEnchantments = new ArrayList<>();
        enchantmentRegistry.asHolderIdMap().forEach(enchantment -> {
            Optional<Identifier> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::identifier);
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

    private boolean matchesClientSearch(String query, Holder<Enchantment> enchantment, Identifier enchantmentId) {
        return EnchantmentSearchRules.matchesAnyCandidate(query, List.of(
                enchantmentId.toString(),
                enchantmentId.getNamespace(),
                enchantmentId.getPath(),
                enchantmentId.getPath().replace('_', ' '),
                enchantment.value().description().getString()
        ));
    }
}
