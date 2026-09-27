package com.river_quinn.enchantment_custom_table.client.gui;

import com.google.gson.*;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.config.*;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/** A small paged editor for structured payment rows; no extra configuration UI dependency. */
public class TableConfigScreen extends Screen {
    private final Screen parent;
    private final boolean readOnly;
    private final List<Row> rows = new ArrayList<>();
    private final boolean[] flags = new boolean[4];
    private static final String[] FLAG_KEYS = {"enforceEnchantmentLevelLimit", "incrementalSameLevelMerge", "convertOnlyLevelOneBook", "freeConversionTableCosts"};
    private final java.util.Map<Button, Component> tooltips = new java.util.LinkedHashMap<>();
    private int page;
    private int rowsPerPage = 3;
    private TableConfigSnapshot displayed;
    private Component status = Component.empty();

    public TableConfigScreen(Screen parent) {
        super(label("title"));
        this.parent = parent;
        Minecraft client = Minecraft.getInstance();
        readOnly = client.getConnection() != null && client.getSingleplayerServer() == null;
        load(readOnly ? Config.snapshot(true) : Config.editableSnapshot());
    }
    private static Component label(String key) { return Component.translatable("enchantment_custom_table.payment_config." + key); }
    private void load(TableConfigSnapshot snapshot) {
        displayed = snapshot; rows.clear();
        snapshot.paymentOptions().forEach((id, cost) -> rows.add(new Row(id, Integer.toString(cost))));
        flags[0] = snapshot.enforceEnchantmentLevelLimit(); flags[1] = snapshot.incrementalSameLevelMerge();
        flags[2] = snapshot.convertOnlyLevelOneBook(); flags[3] = snapshot.freeConversionTableCosts();
    }
    private void rebuildEditor() { clearWidgets(); init(); }
    @Override protected void init() {
        tooltips.clear();
        rowsPerPage = Math.max(1, Math.min(4, (height - 164) / 24));
        page = Math.min(page, Math.max(0, (rows.size() - 1) / rowsPerPage));
        int totalWidth = Math.min(420, width - 20), left = (width - totalWidth) / 2;
        for (int line = 0; line < rowsPerPage; line++) {
            int index = page * rowsPerPage + line;
            if (index >= rows.size()) break;
            Row row = rows.get(index); int y = 52 + line * 24;
            EditBox id = new EditBox(font, left, y, totalWidth - 95, 20, label("item"));
            id.setMaxLength(256); id.setValue(row.id); id.setResponder(value -> row.id = value);
            id.setEditable(!readOnly); addRenderableWidget(id);
            EditBox cost = new EditBox(font, left + totalWidth - 90, y, 60, 20, label("cost"));
            cost.setMaxLength(128); cost.setValue(row.cost); cost.setResponder(value -> row.cost = value);
            cost.setEditable(!readOnly); addRenderableWidget(cost);
            button(left + totalWidth - 25, y, 25, Component.literal("−"), () -> {
                rows.remove(index); rebuildEditor();
            }, !readOnly);
        }
        int controlsY = 52 + rowsPerPage * 24;
        button(left, controlsY, 75, label("add"), () -> {
            rows.add(new Row("minecraft:diamond", "1")); page = (rows.size() - 1) / rowsPerPage; rebuildEditor();
        }, !readOnly && rows.size() < PaymentOptions.MAX_ENTRIES);
        button(left + totalWidth - 100, controlsY, 30, Component.literal("<"), () -> { page--; rebuildEditor(); }, page > 0);
        button(left + totalWidth - 30, controlsY, 30, Component.literal(">"), () -> { page++; rebuildEditor(); }, (page + 1) * rowsPerPage < rows.size());
        for (int i = 0; i < flags.length; i++) {
            int flag = i;
            Component name = label(FLAG_KEYS[i]);
            Button option = button(left + i % 2 * (totalWidth / 2 + 2), controlsY + 24 + i / 2 * 24,
                    totalWidth / 2 - 2, name.copy().append(flags[i] ? ": ON" : ": OFF"), () -> {
                        flags[flag] = !flags[flag]; rebuildEditor();
                    }, !readOnly);
            tooltips.put(option, Component.translatable("enchantment_custom_table.configuration." + FLAG_KEYS[i] + ".description"));
        }
        int footer = height - 26, buttonWidth = Math.min(100, (width - 30) / 3), x = (width - (buttonWidth * 3 + 10)) / 2;
        button(x, footer, buttonWidth, label("reset"), () -> { load(Config.defaultSnapshot()); page = 0; rebuildEditor(); }, !readOnly);
        button(x + buttonWidth + 5, footer, buttonWidth, Component.translatable("gui.cancel"), this::onClose, true);
        button(x + (buttonWidth + 5) * 2, footer, buttonWidth, Component.translatable("gui.done"), this::save, !readOnly);
    }
    private Button button(int x, int y, int width, Component text, Runnable action, boolean enabled) {
        Button widget = new Button(x, y, width, 20, text, b -> action.run());
        widget.active = enabled; return addRenderableWidget(widget);
    }
    private void save() {
        if (readOnly) return;
        JsonObject root = new JsonObject(); JsonArray entries = new JsonArray();
        for (Row row : rows) {
            JsonObject entry = new JsonObject(); entry.addProperty("item_id", row.id);
            try {
                if (!row.cost.matches("-?[0-9]+")) throw new NumberFormatException();
                entry.addProperty("cost", new java.math.BigInteger(row.cost));
            } catch (NumberFormatException ex) { status = label("integer_required"); return; }
            entries.add(entry);
        }
        root.add(JsonTableConfigCodec.PAYMENT_OPTIONS, entries);
        for (int i = 0; i < flags.length; i++) root.addProperty(FLAG_KEYS[i], flags[i]);
        try {
            var parsed = JsonTableConfigCodec.parse(root, Config::warn);
            Config.save(MinecraftPaymentConfig.resolve(parsed, Config::warn));
            onClose();
        } catch (RuntimeException ex) { Config.warn("Cannot save payment config: " + ex.getMessage()); status = label("save_failed"); }
    }
    @Override public void tick() {
        if (readOnly && displayed != Config.snapshot(true)) { load(Config.snapshot(true)); rebuildEditor(); }
    }
    @Override public void render(PoseStack graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawCenteredString(graphics, font, title, width / 2, 10, 0xFFFFFFFF);
        drawCenteredString(graphics, font, readOnly ? label("server_controlled") : status.getString().isEmpty() ? label("hint") : status,
                width / 2, 27, 0xFFFFFF88);
        int totalWidth = Math.min(420, width - 20), left = (width - totalWidth) / 2;
        drawString(graphics, font, label("item"), left, 41, 0xFFFFFFFF);
        drawString(graphics, font, label("cost"), left + totalWidth - 90, 41, 0xFFFFFFFF);
        drawCenteredString(graphics, font, Component.literal(Integer.toString(page + 1)), left + totalWidth - 50,
                58 + rowsPerPage * 24, 0xFFFFFFFF);
        tooltips.forEach((widget, text) -> { if (widget.isMouseOver(mouseX, mouseY)) renderTooltip(graphics, text, mouseX, mouseY); });
    }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    private static final class Row {
        String id; String cost;
        Row(String id, String cost) { this.id = id; this.cost = cost; }
    }
}
