package com.river_quinn.enchantment_custom_table.fabric.transfer;

import com.river_quinn.enchantment_custom_table.core.inventory.AutomationPort;
import com.river_quinn.enchantment_custom_table.core.inventory.SlotRole;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.Iterator;

public class FabricConversionAutomationStorage extends SnapshotParticipant<ItemStack[]> implements Storage<ItemVariant>, AutomationPort {
    private static final int AUTOMATION_BOOK_SLOT = 0;
    private static final int AUTOMATION_PAYMENT_SLOT = 1;
    private static final int AUTOMATION_COPY_RESULT_SLOT = 2;

    private final FabricEnchantmentConversionTableBlockEntity blockEntity;
    private final StorageView<ItemVariant> copyResultView = new CopyResultView();

    public FabricConversionAutomationStorage(FabricEnchantmentConversionTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        int internalSlot = switch (automationSlotForInsert(resource)) {
            case AUTOMATION_BOOK_SLOT -> FabricEnchantmentConversionMenu.BOOK_SLOT;
            case AUTOMATION_PAYMENT_SLOT -> FabricEnchantmentConversionMenu.PAYMENT_SLOT;
            default -> -1;
        };
        if (internalSlot < 0 || maxAmount <= 0) {
            return 0;
        }

        int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        updateSnapshots(transaction);
        ItemStack remainder = blockEntity.getInventory().insertItem(internalSlot, resource.toStack(requested), false);
        return requested - remainder.getCount();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack copyResult = copyResultStack();
        if (maxAmount <= 0 || copyResult.isEmpty() || !resource.matches(copyResult)) {
            return 0;
        }

        int requested = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        updateSnapshots(transaction);
        ItemStack extracted = blockEntity.getInventory().extractItem(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT, requested, false);
        return extracted.getCount();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return copyResultStack().isEmpty() ? Collections.emptyIterator() : Collections.singleton(copyResultView).iterator();
    }

    @Override
    public int getSlots() {
        return 3;
    }

    @Override
    public SlotRole getRole(int slot) {
        return switch (slot) {
            case AUTOMATION_BOOK_SLOT -> SlotRole.BOOK_INPUT;
            case AUTOMATION_PAYMENT_SLOT -> SlotRole.PAYMENT;
            case AUTOMATION_COPY_RESULT_SLOT -> SlotRole.COPY_RESULT;
            default -> SlotRole.GENERATED_BOOK;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack) {
        return switch (slot) {
            case AUTOMATION_BOOK_SLOT -> EnchantmentTableRules.acceptsConversionBookInput(FabricTableConfig.snapshot()) && stack.is(Items.BOOK);
            case AUTOMATION_PAYMENT_SLOT -> blockEntity.isPaymentItem(stack);
            default -> false;
        };
    }

    @Override
    public boolean canExtract(int slot) {
        return slot == AUTOMATION_COPY_RESULT_SLOT;
    }

    @Override
    public long getVersion() {
        return blockEntity.getInventoryVersion();
    }

    private int automationSlotForInsert(ItemVariant resource) {
        if (resource.isBlank()) {
            return -1;
        }
        if (EnchantmentTableRules.acceptsConversionBookInput(FabricTableConfig.snapshot()) && resource.isOf(Items.BOOK)) {
            return AUTOMATION_BOOK_SLOT;
        }
        if (blockEntity.isPaymentItem(resource.toStack())) {
            return AUTOMATION_PAYMENT_SLOT;
        }
        return -1;
    }

    private ItemStack copyResultStack() {
        return blockEntity.getInventory().getStackInSlot(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT);
    }

    @Override
    protected ItemStack[] createSnapshot() {
        ItemStack[] snapshot = new ItemStack[4];
        snapshot[0] = blockEntity.getInventory().getStackInSlot(FabricEnchantmentConversionMenu.BOOK_SLOT).copy();
        snapshot[1] = blockEntity.getInventory().getStackInSlot(FabricEnchantmentConversionMenu.PAYMENT_SLOT).copy();
        snapshot[2] = blockEntity.getInventory().getStackInSlot(FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT).copy();
        snapshot[3] = blockEntity.getInventory().getStackInSlot(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT).copy();
        return snapshot;
    }

    @Override
    protected void readSnapshot(ItemStack[] snapshot) {
        blockEntity.restoreAutomationSnapshot(snapshot);
    }

    private class CopyResultView implements StorageView<ItemVariant> {
        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return FabricConversionAutomationStorage.this.extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return copyResultStack().isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            ItemStack stack = copyResultStack();
            return stack.isEmpty() ? ItemVariant.blank() : ItemVariant.of(stack);
        }

        @Override
        public long getAmount() {
            return copyResultStack().getCount();
        }

        @Override
        public long getCapacity() {
            return 1;
        }
    }
}
