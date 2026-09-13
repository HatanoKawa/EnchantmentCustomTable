package com.river_quinn.enchantment_custom_table.core.inventory;

import com.river_quinn.enchantment_custom_table.core.platform.EnchantmentAccessService;
import com.river_quinn.enchantment_custom_table.core.session.EnchantingTableSession;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class BookInsertionSessionTest {
    protected abstract EnchantmentAccessService enchantments();

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void issue25SplitAndReinsertCannotCreateLevels() {
        assertRoundTrip(3, 1, false, false);
    }

    @Test
    void splitAndReinsertStillWorksAtTheConfiguredLevelLimit() {
        assertRoundTrip(3, 1, true, false);
    }

    @Test
    void incrementalSplitAndReinsertCannotCreateLevels() {
        assertRoundTrip(5, 4, false, true);
    }

    @Test
    void incrementalSplitAndReinsertRespectsTheLevelLimit() {
        assertRoundTrip(3, 2, true, true);
    }

    @Test
    void levelLimitRejectionPreservesSourceAndCarriedBook() {
        assertRejected(3, 1, true, false);
    }

    @Test
    void incrementalMismatchPreservesSourceAndCarriedBook() {
        assertRejected(3, 2, false, true);
    }

    private void assertRoundTrip(int initialLevel, int splitLevel, boolean limit, boolean incremental) {
        TableInventory inventory = new TableInventory();
        inventory.setItem(0, book(initialLevel));
        EnchantingTableSession session = session(inventory, limit, incremental);
        Slot slot = insertionSlot(inventory, session, 2);

        for (int attempt = 0; attempt < 10; attempt++) {
            ItemStack taken = slot.getItem().copy();
            assertEquals(splitLevel, level(taken));
            assertTrue(session.removeGeneratedBookAtCacheIndex(taken, 0).success());
            assertEquals(incremental ? initialLevel - 1 : initialLevel - splitLevel,
                    level(inventory.getItem(0)));

            // Incremental splitting refreshes previews to one level below the remainder.
            // Reinsert into an empty slot so this exercises safeInsert, not the separate swap path.
            Slot target = incremental ? insertionSlot(inventory, session, 4) : slot;
            ItemStack carried = target.safeInsert(taken, 1);

            assertTrue(carried.isEmpty(), "Successful reinsertion must consume the extracted book");
            assertEquals(initialLevel, level(inventory.getItem(0)), "A split/merge round trip must not add levels");
            target.safeInsert(carried, 1);
            assertEquals(initialLevel, level(inventory.getItem(0)));
        }
    }

    private void assertRejected(int initialLevel, int offeredLevel, boolean limit, boolean incremental) {
        TableInventory inventory = new TableInventory();
        inventory.setItem(0, book(initialLevel));
        EnchantingTableSession session = session(inventory, limit, incremental);
        Slot slot = insertionSlot(inventory, session, 2);
        ItemStack carried = book(offeredLevel);
        ItemStack preview = slot.getItem().copy();

        assertSame(carried, slot.safeInsert(carried, 1));

        assertEquals(1, carried.getCount());
        assertEquals(initialLevel, level(inventory.getItem(0)));
        assertTrue(ItemStack.matches(preview, slot.getItem()));
    }

    private EnchantingTableSession session(TableInventory inventory, boolean limit, boolean incremental) {
        // Legacy enchantment adapters use static registries and do not need a running world here.
        EnchantingTableSession session = new EnchantingTableSession(null, inventory, enchantments(),
                () -> new EnchantmentTableRules.MergeOptions(limit, incremental), 0, 1, 2, 24);
        session.refreshGeneratedSlotsFromTool();
        return session;
    }

    private static Slot insertionSlot(TableInventory inventory, EnchantingTableSession session, int index) {
        return new Slot(inventory, index, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return session.canApplyEnchantedBook(stack);
            }

            @Override
            public ItemStack safeInsert(ItemStack stack, int amount) {
                return GeneratedBookInsertion.insert(this, stack, amount,
                        book -> session.applyEnchantedBook(book).success());
            }
        };
    }

    private static ItemStack book(int level) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.MOB_LOOTING, level));
    }

    private static int level(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack).getOrDefault(Enchantments.MOB_LOOTING, 0);
    }

    private static final class TableInventory extends SimpleContainer implements LogicalInventory {
        private TableInventory() {
            super(26);
        }

        @Override
        public int getSlots() {
            return getContainerSize();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return getItem(slot);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            setItem(slot, stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            throw new UnsupportedOperationException("Session must not use automation insertion");
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            throw new UnsupportedOperationException("Session must not use automation extraction");
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }
}
