package com.river_quinn.enchantment_custom_table.fabric.block.entity;

import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import com.river_quinn.enchantment_custom_table.core.session.TableOperationResult;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.inventory.FabricTableInventory;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlockEntities;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantmentConversionMenu;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import com.river_quinn.enchantment_custom_table.fabric.transfer.FabricConversionAutomationStorage;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEnchantmentConversionTableBlockEntity extends FabricEnchantingTableLikeBlockEntity {
    private boolean updatingCopyResult = false;
    private boolean loadingInventory = false;
    private int inventoryVersion = 0;
    private final FabricTableInventory inventory = new FabricTableInventory(
            FabricEnchantmentConversionMenu.ENCHANTMENT_CONVERSION_SLOT_SIZE,
            slot -> switch (slot) {
                case FabricEnchantmentConversionMenu.BOOK_SLOT, FabricEnchantmentConversionMenu.PAYMENT_SLOT -> 64;
                case FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT, FabricEnchantmentConversionMenu.COPY_RESULT_SLOT -> 1;
                default -> 1;
            },
            this::isItemValid,
            this::markInventoryChanged
    );
    private final Storage<ItemVariant> automationStorage = new FabricConversionAutomationStorage(this);

    public FabricEnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricModBlockEntities.ENCHANTMENT_CONVERSION_TABLE, pos, state);
    }

    public FabricTableInventory getInventory() {
        return inventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    public Storage<ItemVariant> getAutomationStorage() {
        return automationStorage;
    }

    public boolean isCopyMode() {
        return isValidCopyTemplate(inventory.getStackInSlot(FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT));
    }

    public boolean isPaymentItem(ItemStack stack) {
        return EnchantmentTableRules.paymentCostFor(stack.getItem(), FabricTableConfig.snapshot()) > 0;
    }

    public boolean isValidCopyTemplate(ItemStack stack) {
        if (level == null || !stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }
        EnchantmentList enchantments = FabricEnchantmentUtils.getEnchantments(stack);
        if (enchantments.size() != 1) {
            return false;
        }
        EnchantmentEntry entry = enchantments.entries().get(0);
        return EnchantmentTableRules.isValidSingleEnchantmentTemplate(
                enchantments.size(),
                entry.level(),
                entry.maxLevel()
        );
    }

    public void refreshCopyResult() {
        if (updatingCopyResult) {
            return;
        }
        updatingCopyResult = true;
        TableOperationResult result;
        try {
            result = ConversionTableSession.refreshCopyResult(
                    inventory,
                    this::isCopyMode,
                    FabricTableConfig::snapshot,
                    FabricEnchantmentConversionMenu.BOOK_SLOT,
                    FabricEnchantmentConversionMenu.PAYMENT_SLOT,
                    FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT,
                    FabricEnchantmentConversionMenu.COPY_RESULT_SLOT
            );
        } finally {
            updatingCopyResult = false;
        }
        if (result.changed()) {
            markInventoryChanged(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT);
        }
    }

    private boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case FabricEnchantmentConversionMenu.BOOK_SLOT -> stack.is(Items.BOOK);
            case FabricEnchantmentConversionMenu.PAYMENT_SLOT -> isPaymentItem(stack);
            case FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT -> isValidCopyTemplate(stack);
            default -> false;
        };
    }

    private void markInventoryChanged(int slot) {
        if (slot >= FabricEnchantmentConversionMenu.ENCHANTED_BOOK_SLOT_START
                && slot < FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT) {
            return;
        }
        inventoryVersion++;
        if (!updatingCopyResult && !loadingInventory) {
            refreshCopyResult();
        }
        setChanged();
    }

    public void restoreAutomationSnapshot(ItemStack[] snapshot) {
        loadingInventory = true;
        try {
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.BOOK_SLOT, snapshot[0]);
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.PAYMENT_SLOT, snapshot[1]);
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT, snapshot[2]);
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT, snapshot[3]);
        } finally {
            loadingInventory = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        NonNullList<ItemStack> persistentItems = NonNullList.withSize(4, ItemStack.EMPTY);
        persistentItems.set(0, inventory.getStackInSlot(FabricEnchantmentConversionMenu.BOOK_SLOT));
        persistentItems.set(1, inventory.getStackInSlot(FabricEnchantmentConversionMenu.PAYMENT_SLOT));
        persistentItems.set(2, inventory.getStackInSlot(FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT));
        persistentItems.set(3, inventory.getStackInSlot(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT));
        tag.put("Inventory", ContainerHelper.saveAllItems(new CompoundTag(), persistentItems));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        NonNullList<ItemStack> persistentItems = NonNullList.withSize(4, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Inventory"), persistentItems);
        loadingInventory = true;
        try {
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.BOOK_SLOT, persistentItems.get(0));
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.PAYMENT_SLOT, persistentItems.get(1));
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.TEMPLATE_BOOK_SLOT, persistentItems.get(2));
            inventory.setStackInSlot(FabricEnchantmentConversionMenu.COPY_RESULT_SLOT, persistentItems.get(3));
        } finally {
            loadingInventory = false;
        }
    }
}
