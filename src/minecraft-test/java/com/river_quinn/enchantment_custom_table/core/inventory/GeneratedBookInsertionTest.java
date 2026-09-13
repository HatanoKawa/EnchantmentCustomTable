package com.river_quinn.enchantment_custom_table.core.inventory;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedBookInsertionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void vanillaFullBookInsertionStillCallsSetWithoutConsumingCarriedBook() {
        AtomicInteger writes = new AtomicInteger();
        SimpleContainer container = new SimpleContainer(book());
        Slot vanillaSlot = new Slot(container, 0, 0, 0) {
            @Override
            public void set(ItemStack stack) {
                writes.incrementAndGet();
                super.set(stack);
            }
        };
        ItemStack carried = book();

        ItemStack remaining = vanillaSlot.safeInsert(carried, 1);

        assertEquals(1, writes.get(), "Vanilla writes the unchanged preview even when zero books fit");
        assertSame(carried, remaining);
        assertEquals(1, remaining.getCount());
    }

    @Test
    void identicalPreviewInsertionConsumesBookBeforeVanillaReassignsCarried() {
        InsertionSlot slot = new InsertionSlot(book());
        ItemStack carried = book();

        carried = slot.safeInsert(carried, 1);

        assertTrue(carried.isEmpty());
        assertEquals(1, slot.applications);
        assertEquals(1, slot.getItem().getCount(), "Do not grow or consume the preview directly");
        for (int i = 0; i < 10; i++) {
            carried = slot.safeInsert(carried, 1);
        }
        assertEquals(1, slot.applications, "The consumed book cannot be reused to increase levels");
    }

    @Test
    void emptyPreviewAlsoConsumesExactlyOneBook() {
        InsertionSlot slot = new InsertionSlot(ItemStack.EMPTY);
        ItemStack carried = book();
        carried.setCount(3);

        assertSame(carried, slot.safeInsert(carried));
        assertEquals(2, carried.getCount());
        assertEquals(1, slot.applications);
    }

    @Test
    void zeroAndNegativeRequestsDoNotApplyEnchantment() {
        InsertionSlot slot = new InsertionSlot(book());
        ItemStack carried = book();

        slot.safeInsert(carried, 0);
        slot.safeInsert(carried, -1);

        assertEquals(0, slot.applications);
        assertEquals(1, carried.getCount());
    }

    @Test
    void rejectedMergeDoesNotConsumeBook() {
        InsertionSlot slot = new InsertionSlot(book());
        slot.acceptOperation = false;
        ItemStack carried = book();

        assertSame(carried, slot.safeInsert(carried, 1));
        assertEquals(1, carried.getCount());
        assertEquals(0, slot.applications);
    }

    @Test
    void placementRulesRemainAuthoritative() {
        InsertionSlot slot = new InsertionSlot(book());
        slot.allowPlacement = false;
        ItemStack carried = book();

        slot.safeInsert(carried, 1);

        assertEquals(0, slot.attempts);
        assertEquals(1, carried.getCount());
    }

    @Test
    void otherBooksUseTheExistingSwapPathInstead() {
        InsertionSlot slot = new InsertionSlot(book());
        ItemStack differentBook = EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(Enchantments.SHARPNESS, 1));

        slot.safeInsert(differentBook, 1);
        slot.safeInsert(new ItemStack(Items.BOOK), 1);

        assertEquals(0, slot.attempts);
        assertEquals(1, differentBook.getCount());
    }

    private static ItemStack book() {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.MOB_LOOTING, 1));
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
        public void set(ItemStack stack) {
            fail("Insertion must not reinterpret a preview write as another input book");
        }
    }
}
