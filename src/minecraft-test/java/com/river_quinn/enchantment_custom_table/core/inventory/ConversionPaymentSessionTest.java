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

    private void assertPaymentPersisted(Item payment, int cost) {
        AtomicBoolean dirty = new AtomicBoolean();
        LogicalInventory inventory = inventory(slot -> { if (slot < 2) dirty.set(true); });
        inventory.setStackInSlot(0, new ItemStack(Items.BOOK, 2));
        inventory.setStackInSlot(1, new ItemStack(payment, cost));
        ItemStack[] saved = {inventory.getStackInSlot(0).copy(), inventory.getStackInSlot(1).copy()};
        dirty.set(false);
        ConversionTableSession session = new ConversionTableSession(null, inventory, enchantments(),
                () -> false, () -> new TableConfigSnapshot(36, 4, false, false, false, false), 0, 1, 2, 1);

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
