package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.core.inventory.GeneratedBookInsertion;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.session.EnchantingTableSession;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.river_quinn.enchantment_custom_table.fabric.screen.MinecraftBookTestData.*;
import static org.junit.jupiter.api.Assertions.*;

class BookInsertionSessionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        book(1);
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
    void levelLimitRejectionPreservesSourceAndInput() {
        assertRejected(3, book(1), true, false);
    }

    @Test
    void incrementalMismatchPreservesSourceAndInput() {
        assertRejected(3, book(2), false, true);
    }

    @Test
    void multiEnchantmentInputIsRejectedAsAWhole() {
        ItemStack offered = book(1);
        offered.enchant(SHARPNESS, 1);

        assertRejected(3, offered, true, false);
    }

    @Test
    void mergingThroughAnOccupiedToolPreviewConsumesTheBook() {
        ItemStack tool = new ItemStack(Items.WOODEN_SWORD);
        tool.enchant(LOOTING, 1);
        TableInventory inventory = new TableInventory();
        inventory.setItem(0, tool);
        EnchantingTableSession session = session(inventory, false, false);
        Slot slot = insertionSlot(inventory, session, 2);
        ItemStack carried = book(1);
        assertTrue(ItemStack.isSameItemSameComponents(slot.getItem(), carried));

        assertTrue(slot.safeInsert(carried, 1).isEmpty());

        assertEquals(2, level(inventory.getItem(0)));
        assertEquals(2, level(slot.getItem()));
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
            // Use an empty slot for reinsertion; a different preview uses the separate swap path.
            Slot target = incremental ? insertionSlot(inventory, session, 4) : slot;
            assertTrue(target.safeInsert(taken, 1).isEmpty());
            assertEquals(initialLevel, level(inventory.getItem(0)), "Split/merge must conserve levels");
            target.safeInsert(taken, 1);
            assertEquals(initialLevel, level(inventory.getItem(0)));
        }
    }

    private void assertRejected(int initialLevel, ItemStack offered, boolean limit, boolean incremental) {
        TableInventory inventory = new TableInventory();
        inventory.setItem(0, book(initialLevel));
        EnchantingTableSession session = session(inventory, limit, incremental);
        Slot slot = insertionSlot(inventory, session, 4);
        ItemStack originalTool = inventory.getItem(0).copy();

        assertSame(offered, slot.safeInsert(offered, 1));

        assertEquals(1, offered.getCount());
        assertTrue(ItemStack.matches(originalTool, inventory.getItem(0)));
        assertTrue(slot.getItem().isEmpty());
    }

    private EnchantingTableSession session(TableInventory inventory, boolean limit, boolean incremental) {
        // Real, bound vanilla holders let the adapter's null-world fallback run without a server.
        EnchantingTableSession session = new EnchantingTableSession(null, inventory, FabricEnchantmentUtils.service(),
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
