package com.river_quinn.enchantment_custom_table.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class TableConfigScreen extends Screen {
    private static final int MIN_COST = 0;
    private static final int MAX_COST = 64;

    private final Screen parent;
    private final Map<AbstractWidget, Component> optionTooltips = new LinkedHashMap<>();
    private int minimumEmeraldCost;
    private int minimumEmeraldBlockCost;
    private boolean enforceEnchantmentLevelLimit;
    private boolean incrementalSameLevelMerge;
    private boolean convertOnlyLevelOneBook;
    private boolean freeConversionTableCosts;

    public TableConfigScreen(Screen parent) {
        super(new TranslatableComponent(
                "enchantment_custom_table.configuration.title",
                new TextComponent("Enchantment Custom Table")
        ));
        this.parent = parent;
        load(Config.snapshot());
    }

    @Override
    protected void init() {
        optionTooltips.clear();
        int controlWidth = Math.min(150, Math.max(100, (width - 30) / 2));
        int left = (width - controlWidth * 2 - 10) / 2;
        int right = left + controlWidth + 10;
        int firstRow = 46;
        int rowSpacing = 24;

        addIntegerOption(
                left,
                firstRow,
                controlWidth,
                "enchantment_custom_table.configuration.minimumEmeraldCost",
                "enchantment_custom_table.configuration.minimumEmeraldCost.description",
                minimumEmeraldCost,
                value -> minimumEmeraldCost = value
        );
        addIntegerOption(
                right,
                firstRow,
                controlWidth,
                "enchantment_custom_table.configuration.minimumEmeraldBlockCost",
                "enchantment_custom_table.configuration.minimumEmeraldBlockCost.description",
                minimumEmeraldBlockCost,
                value -> minimumEmeraldBlockCost = value
        );
        addBooleanOption(
                left,
                firstRow + rowSpacing,
                controlWidth,
                "enchantment_custom_table.configuration.enforceEnchantmentLevelLimit",
                "enchantment_custom_table.configuration.enforceEnchantmentLevelLimit.description",
                enforceEnchantmentLevelLimit,
                value -> enforceEnchantmentLevelLimit = value
        );
        addBooleanOption(
                right,
                firstRow + rowSpacing,
                controlWidth,
                "enchantment_custom_table.configuration.incrementalSameLevelMerge",
                "enchantment_custom_table.configuration.incrementalSameLevelMerge.description",
                incrementalSameLevelMerge,
                value -> incrementalSameLevelMerge = value
        );
        addBooleanOption(
                left,
                firstRow + rowSpacing * 2,
                controlWidth,
                "enchantment_custom_table.configuration.convertOnlyLevelOneBook",
                "enchantment_custom_table.configuration.convertOnlyLevelOneBook.description",
                convertOnlyLevelOneBook,
                value -> convertOnlyLevelOneBook = value
        );
        addBooleanOption(
                right,
                firstRow + rowSpacing * 2,
                controlWidth,
                "enchantment_custom_table.configuration.freeConversionTableCosts",
                "enchantment_custom_table.configuration.freeConversionTableCosts.description",
                freeConversionTableCosts,
                value -> freeConversionTableCosts = value
        );

        int buttonWidth = Math.min(100, Math.max(80, (width - 40) / 3));
        int buttonGap = 5;
        int buttonRowWidth = buttonWidth * 3 + buttonGap * 2;
        int buttonX = (width - buttonRowWidth) / 2;
        int buttonY = height - 28;
        addRenderableWidget(new Button(buttonX, buttonY, buttonWidth, 20,
                new TranslatableComponent("controls.reset"), button -> resetToDefaults()));
        addRenderableWidget(new Button(buttonX + buttonWidth + buttonGap, buttonY, buttonWidth, 20,
                CommonComponents.GUI_CANCEL, button -> onClose()));
        addRenderableWidget(new Button(buttonX + (buttonWidth + buttonGap) * 2, buttonY, buttonWidth, 20,
                CommonComponents.GUI_DONE, button -> saveAndClose()));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, font, title, width / 2, 18, 0xFFFFFF);
        for (Map.Entry<AbstractWidget, Component> entry : optionTooltips.entrySet()) {
            if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                renderTooltip(poseStack, entry.getValue(), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    private void addIntegerOption(
            int x,
            int y,
            int controlWidth,
            String labelKey,
            String descriptionKey,
            int initialValue,
            IntConsumer onChange
    ) {
        IntegerSlider slider = new IntegerSlider(
                x,
                y,
                controlWidth,
                new TranslatableComponent(labelKey),
                initialValue,
                onChange
        );
        optionTooltips.put(slider, new TranslatableComponent(descriptionKey));
        addRenderableWidget(slider);
    }

    private void addBooleanOption(
            int x,
            int y,
            int controlWidth,
            String labelKey,
            String descriptionKey,
            boolean initialValue,
            Consumer<Boolean> onChange
    ) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(initialValue).create(
                x,
                y,
                controlWidth,
                20,
                new TranslatableComponent(labelKey),
                (cycleButton, value) -> onChange.accept(value)
        );
        optionTooltips.put(button, new TranslatableComponent(descriptionKey));
        addRenderableWidget(button);
    }

    private void resetToDefaults() {
        load(Config.defaultSnapshot());
        clearWidgets();
        init();
    }

    private void saveAndClose() {
        Config.save(new TableConfigSnapshot(
                minimumEmeraldCost,
                minimumEmeraldBlockCost,
                enforceEnchantmentLevelLimit,
                incrementalSameLevelMerge,
                convertOnlyLevelOneBook,
                freeConversionTableCosts
        ));
        onClose();
    }

    private void load(TableConfigSnapshot snapshot) {
        minimumEmeraldCost = snapshot.minimumEmeraldCost();
        minimumEmeraldBlockCost = snapshot.minimumEmeraldBlockCost();
        enforceEnchantmentLevelLimit = snapshot.enforceEnchantmentLevelLimit();
        incrementalSameLevelMerge = snapshot.incrementalSameLevelMerge();
        convertOnlyLevelOneBook = snapshot.convertOnlyLevelOneBook();
        freeConversionTableCosts = snapshot.freeConversionTableCosts();
    }

    private static final class IntegerSlider extends AbstractSliderButton {
        private final Component label;
        private final IntConsumer onChange;

        private IntegerSlider(
                int x,
                int y,
                int width,
                Component label,
                int initialValue,
                IntConsumer onChange
        ) {
            super(x, y, width, 20, TextComponent.EMPTY, normalize(initialValue));
            this.label = label;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(label.copy().append(": ").append(Integer.toString(currentValue())));
        }

        @Override
        protected void applyValue() {
            onChange.accept(currentValue());
        }

        private int currentValue() {
            return MIN_COST + (int) Math.round(value * (MAX_COST - MIN_COST));
        }

        private static double normalize(int value) {
            int clamped = Math.max(MIN_COST, Math.min(MAX_COST, value));
            return (double) (clamped - MIN_COST) / (MAX_COST - MIN_COST);
        }
    }
}
