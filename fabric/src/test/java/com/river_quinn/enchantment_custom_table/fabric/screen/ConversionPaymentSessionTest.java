package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentKey;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigSnapshot;
import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ConversionPaymentSessionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        MinecraftBookTestData.book(1);
    }

    @Test
    void emeraldPurchaseNotifiesPersistentInventoryAfterAnEarlierSave() {
        assertPaymentPersisted(Items.EMERALD, 36);
    }

    @Test
    void blockPurchaseNotifiesPersistentInventoryAfterAnEarlierSave() {
        assertPaymentPersisted(Items.EMERALD_BLOCK, 4);
    }

    private void assertPaymentPersisted(net.minecraft.world.item.Item payment, int cost) {
        AtomicBoolean dirty = new AtomicBoolean();
        FabricTableInventory inventory = new FabricTableInventory(3, slot -> 64, (slot, stack) -> true,
                slot -> { if (slot < 2) dirty.set(true); });
        inventory.setStackInSlot(0, new ItemStack(Items.BOOK, 2));
        inventory.setStackInSlot(1, new ItemStack(payment, cost));
        ItemStack[] saved = {inventory.getStackInSlot(0).copy(), inventory.getStackInSlot(1).copy()};
        dirty.set(false); // The chunk has already been saved after filling the table.
        ConversionTableSession session = new ConversionTableSession(null, inventory, ENCHANTMENTS,
                () -> false, () -> new TableConfigSnapshot(36, 4, false, false, false, false), 0, 1, 2, 1);

        assertTrue(session.pickGeneratedBook().success());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertTrue(inventory.getStackInSlot(1).isEmpty());
        // A clean chunk is not written again, even if its live ItemStacks were mutated.
        if (dirty.get()) {
            saved[0] = inventory.getStackInSlot(0).copy();
            saved[1] = inventory.getStackInSlot(1).copy();
        }
        assertEquals(1, saved[0].getCount(), "Reload must not restore a spent book");
        assertTrue(saved[1].isEmpty(), "Reload must not restore spent payment");
    }

    private static final EnchantmentAccessService ENCHANTMENTS = new EnchantmentAccessService() {
        @Override
        public List<Holder<Enchantment>> allEnchantments(Level level) {
            return List.of(MinecraftBookTestData.LOOTING);
        }

        @Override
        public Optional<Holder<Enchantment>> resolveEnchantmentHolder(Level level, Holder<Enchantment> enchantment) {
            return Optional.of(enchantment);
        }

        @Override
        public ItemEnchantments getEnchantments(ItemStack stack) {
            return EnchantmentHelper.getEnchantmentsForCrafting(stack);
        }

        @Override
        public EnchantmentKey getCoreEnchantmentKey(Level level, Holder<Enchantment> enchantment) {
            return new EnchantmentKey("minecraft", "looting");
        }

        @Override
        public void setEnchantments(ItemStack stack, ItemEnchantments enchantments) {
            EnchantmentHelper.setEnchantments(stack, enchantments);
        }
    };
}
