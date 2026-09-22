package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.core.inventory.GeneratedBookInsertion;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.river_quinn.enchantment_custom_table.fabric.screen.MinecraftBookTestData.book;
import static org.junit.jupiter.api.Assertions.*;

class GeneratedBookInsertionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        book(1);
    }

    @Test
    void vanillaFullBookInsertionHasVersionDependentCallbacksButNeverConsumesInput() {
        AtomicInteger writes = new AtomicInteger();
        Slot vanillaSlot = new Slot(new SimpleContainer(book(1)), 0, 0, 0) {
            @Override
            public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                writes.incrementAndGet();
                super.setByPlayer(newStack, oldStack);
            }
        };
        ItemStack carried = book(1);

        assertSame(carried, vanillaSlot.safeInsert(carried, 1));

        String version = System.getProperty("enchantment_custom_table.test.minecraftVersion");
        assertNotNull(version, "The version project must identify its Minecraft runtime");
        assertEquals(version.equals("1.21.1") ? 1 : 0, writes.get(),
                "1.21.1 writes the full preview; newer versions skip the insertion entirely");
        assertEquals(1, carried.getCount());
    }

    @Test
    void identicalPreviewConsumesInputBeforeVanillaReassignsCarried() {
        InsertionSlot slot = new InsertionSlot(book(1));
        ItemStack carried = book(1);

        carried = slot.safeInsert(carried, 1);

        assertTrue(carried.isEmpty());
        assertEquals(1, slot.applications);
        assertEquals(1, slot.getItem().getCount(), "The preview must not be grown or consumed directly");
        for (int i = 0; i < 10; i++) {
            carried = slot.safeInsert(carried, 1);
        }
        assertEquals(1, slot.applications, "A consumed book cannot be reused to add levels");
    }

    @Test
    void emptyPreviewConsumesExactlyOneBookEvenForALargerOfferedStack() {
        InsertionSlot slot = new InsertionSlot(ItemStack.EMPTY);
        ItemStack carried = book(1).copyWithCount(3);

        assertSame(carried, slot.safeInsert(carried));

        assertEquals(2, carried.getCount());
        assertEquals(1, slot.applications);
        assertTrue(slot.getItem().isEmpty());
    }

    @Test
    void rightClickRequestAlsoConsumesOnlyOneBook() {
        InsertionSlot slot = new InsertionSlot(book(1));
        ItemStack carried = book(1).copyWithCount(3);

        slot.safeInsert(carried, 1);

        assertEquals(2, carried.getCount());
        assertEquals(1, slot.applications);
    }

    @Test
    void zeroAndNegativeRequestsDoNotApplyEnchantments() {
        InsertionSlot slot = new InsertionSlot(book(1));
        ItemStack carried = book(1);

        slot.safeInsert(carried, 0);
        slot.safeInsert(carried, -1);

        assertEquals(0, slot.attempts);
        assertEquals(1, carried.getCount());
    }

    @Test
    void failedOperationDoesNotConsumeInput() {
        InsertionSlot slot = new InsertionSlot(book(1));
        slot.acceptOperation = false;
        ItemStack carried = book(1);

        assertSame(carried, slot.safeInsert(carried, 1));

        assertEquals(1, carried.getCount());
        assertEquals(1, slot.attempts);
        assertEquals(0, slot.applications);
    }

    @Test
    void placementRulesRemainAuthoritative() {
        InsertionSlot slot = new InsertionSlot(book(1));
        slot.allowPlacement = false;
        ItemStack carried = book(1);

        slot.safeInsert(carried, 1);

        assertEquals(0, slot.attempts);
        assertEquals(1, carried.getCount());
    }

    @Test
    void differentComponentsRemainOnTheExistingSwapPath() {
        InsertionSlot slot = new InsertionSlot(book(1));
        ItemStack differentBook = book(2);

        slot.safeInsert(differentBook, 1);
        slot.safeInsert(new ItemStack(Items.BOOK), 1);

        assertEquals(0, slot.attempts);
        assertEquals(1, differentBook.getCount());
    }

    private static final class InsertionSlot extends Slot {
        private boolean allowPlacement = true;
        private boolean acceptOperation = true;
        private int attempts;
        private int applications;

        private InsertionSlot(ItemStack preview) {
            super(new SimpleContainer(preview), 0, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return allowPlacement;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack, int amount) {
            return GeneratedBookInsertion.insert(this, stack, amount, input -> {
                attempts++;
                assertEquals(1, input.getCount());
                assertNotSame(stack, input);
                if (acceptOperation) {
                    applications++;
                }
                return acceptOperation;
            });
        }

        @Override
        public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
            fail("Insertion must not interpret a preview write as another input book");
        }
    }
}
