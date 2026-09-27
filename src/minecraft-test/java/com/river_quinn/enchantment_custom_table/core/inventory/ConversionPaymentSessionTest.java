package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import com.river_quinn.enchantment_custom_table.core.config.MinecraftPaymentConfig;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ConversionPaymentSessionTest {
    protected abstract LogicalInventory inventory(IntConsumer changed);

    protected abstract EnchantmentAccessService enchantments();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void emeraldPurchaseNotifiesPersistentInventoryAfterAnEarlierSave() {
        assertPaymentPersisted(Items.EMERALD, 36);
    }

    @Test
    void blockPurchaseNotifiesPersistentInventoryAfterAnEarlierSave() {
        assertPaymentPersisted(Items.EMERALD_BLOCK, 4);
    }

    @Test
    void defaultNetherStarPurchasePersists() {
        assertPaymentPersisted(Items.NETHER_STAR, 1);
    }

    @Test
    void registryResolutionUsesRealStackLimitsAndIgnoresBooksAndMissingMods() {
        var raw = new TableConfigSnapshot(Map.of("minecraft:ender_pearl", 64, "minecraft:diamond_sword", 64,
                "minecraft:book", 1, "missing_mod:token", 1), false, false, false, false);
        var effective = MinecraftPaymentConfig.resolve(raw, message -> {});
        assertEquals(Map.of("minecraft:ender_pearl", 16, "minecraft:diamond_sword", 1), effective.paymentOptions());
        assertFalse(EnchantmentTableRules.hasRequiredConversionMaterials(new ItemStack(Items.BOOK), new ItemStack(Items.ENDER_PEARL, 15), effective));
        assertTrue(EnchantmentTableRules.hasRequiredConversionMaterials(new ItemStack(Items.BOOK), new ItemStack(Items.ENDER_PEARL, 16), effective));
    }

    @Test
    void customPaymentCopyChargesOnceAndFreeModeConsumesNothing() {
        LogicalInventory inventory = inventory(slot -> {});
        var config = new TableConfigSnapshot(Map.of("minecraft:diamond", 3), false, false, false, false);
        inventory.setStackInSlot(0, new ItemStack(Items.BOOK, 2));
        inventory.setStackInSlot(1, new ItemStack(Items.DIAMOND, 6));
        inventory.setStackInSlot(2, enchantments().createEnchantedBook(net.minecraft.world.item.enchantment.Enchantments.MOB_LOOTING, 2));
        assertTrue(ConversionTableSession.refreshCopyResult(inventory, () -> true, () -> config, 0, 1, 2, 3).success());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(3, inventory.getStackInSlot(1).getCount());
        assertEquals(2, net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(inventory.getStackInSlot(3)).get(net.minecraft.world.item.enchantment.Enchantments.MOB_LOOTING));
        assertFalse(ConversionTableSession.refreshCopyResult(inventory, () -> true, () -> config, 0, 1, 2, 3).success());
        assertEquals(3, inventory.getStackInSlot(1).getCount());
        inventory.setStackInSlot(3, ItemStack.EMPTY);
        var free = new TableConfigSnapshot(config.paymentOptions(), false, false, false, true);
        assertTrue(ConversionTableSession.refreshCopyResult(inventory, () -> true, () -> free, 0, 1, 2, 3).success());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(3, inventory.getStackInSlot(1).getCount());
    }

    @Test
    void aChangedServerPriceIsRecheckedBeforeTakingAPreview() {
        LogicalInventory inventory = inventory(slot -> {});
        inventory.setStackInSlot(0, new ItemStack(Items.BOOK, 2));
        inventory.setStackInSlot(1, new ItemStack(Items.DIAMOND, 3));
        var rules = new java.util.concurrent.atomic.AtomicReference<>(new TableConfigSnapshot(Map.of("minecraft:diamond", 3), false, false, false, false));
        var session = new ConversionTableSession(null, inventory, enchantments(), () -> false, rules::get, 0, 1, 2, 1);
        session.regenerateGeneratedSlots();
        assertFalse(inventory.getStackInSlot(2).isEmpty());
        rules.set(new TableConfigSnapshot(Map.of("minecraft:diamond", 4), false, false, false, false));
        assertFalse(session.pickGeneratedBook().success());
        assertTrue(inventory.getStackInSlot(2).isEmpty());
        assertEquals(2, inventory.getStackInSlot(0).getCount());
        assertEquals(3, inventory.getStackInSlot(1).getCount());
    }

    private void assertPaymentPersisted(Item payment, int cost) {
        AtomicBoolean dirty = new AtomicBoolean();
        LogicalInventory inventory = inventory(slot -> { if (slot < 2) dirty.set(true); });
        inventory.setStackInSlot(0, new ItemStack(Items.BOOK, 2));
        inventory.setStackInSlot(1, new ItemStack(payment, cost));
        ItemStack[] saved = {inventory.getStackInSlot(0).copy(), inventory.getStackInSlot(1).copy()};
        dirty.set(false);
        ConversionTableSession session = new ConversionTableSession(null, inventory, enchantments(),
                () -> false, com.river_quinn.enchantment_custom_table.core.config.JsonTableConfigCodec::defaultSnapshot, 0, 1, 2, 1);

        assertTrue(session.pickGeneratedBook().success());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertTrue(inventory.getStackInSlot(1).isEmpty());
        // A previously saved chunk is written again only after the persistent inventory changes.
        if (dirty.get()) {
            saved[0] = inventory.getStackInSlot(0).copy();
            saved[1] = inventory.getStackInSlot(1).copy();
        }
        assertEquals(1, saved[0].getCount(), "Reload must not restore a spent book");
        assertTrue(saved[1].isEmpty(), "Reload must not restore spent payment");
    }
}
