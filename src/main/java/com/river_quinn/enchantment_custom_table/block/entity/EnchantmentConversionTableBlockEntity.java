package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.inventory.AutomationPort;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.inventory.SlotRole;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import com.river_quinn.enchantment_custom_table.core.session.TableOperationResult;
import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
//? if >=1.21.9 {
import com.river_quinn.enchantment_custom_table.world.inventory.MenuItemStackHandler;
import com.river_quinn.enchantment_custom_table.world.inventory.ResourceHandlerLogicalInventory;
//?} else {
/*import com.river_quinn.enchantment_custom_table.world.inventory.ItemHandlerLogicalInventory;
*///?}
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
//? if <1.21.6 {
/*import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
*///?}
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
//? if <1.21.9 {
/*import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
*///?}
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
//?}
import org.jetbrains.annotations.Nullable;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int BOOK_SLOT = 0;
    public static final int PAYMENT_SLOT = 1;
    public static final int TEMPLATE_SLOT = 2;
    public static final int COPY_RESULT_SLOT = 3;
    public static final int SLOT_COUNT = 4;

    //? if >=1.21.9 {
    private final MenuItemStackHandler inventory = new MenuItemStackHandler(SLOT_COUNT, 64) {
        @Override
        public boolean isValid(int slot, ItemResource resource) {
            if (resource.isEmpty()) {
                return false;
            }
            ItemStack stack = resource.toStack();
            return switch (slot) {
                case BOOK_SLOT -> stack.is(Items.BOOK);
                case PAYMENT_SLOT -> isPaymentItem(stack);
                case TEMPLATE_SLOT -> isValidCopyTemplate(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            markInventoryChanged();
            if (!updatingCopyResult) {
                refreshCopyResult();
            }
        }
    };
    private final LogicalInventory logicalInventory = new ResourceHandlerLogicalInventory(inventory);
    //?} else {
    /*private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case BOOK_SLOT -> stack.is(Items.BOOK);
                case PAYMENT_SLOT -> isPaymentItem(stack);
                case TEMPLATE_SLOT -> isValidCopyTemplate(stack);
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            markInventoryChanged();
            if (!updatingCopyResult) {
                refreshCopyResult();
            }
        }
    };
    private final LogicalInventory logicalInventory = new ItemHandlerLogicalInventory(inventory);
    *///?}
    //? if >=1.21.9 {
    private final ResourceHandler<ItemResource> automationHandler = new ConversionAutomationItemHandler();
    //?} else {
    /*private final IItemHandler automationHandler = new ConversionAutomationItemHandler();
    *///?}
    private boolean updatingCopyResult = false;
    private int inventoryVersion = 0;

    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(), pos, state);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchantmentConversionMenu(i, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    //? if >=1.21.9 {
    public MenuItemStackHandler getInventory() {
        return inventory;
    }
    //?} else {
    /*public ItemStackHandler getInventory() {
        return inventory;
    }
    *///?}

    public LogicalInventory getLogicalInventory() {
        return logicalInventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    //? if >=1.21.9 {
    public @Nullable ResourceHandler<ItemResource> getAutomationItemHandler(@Nullable Direction direction) {
        return automationHandler;
    }
    //?} else {
    /*public @Nullable IItemHandler getAutomationItemHandler(@Nullable Direction direction) {
        return automationHandler;
    }
    *///?}

    public boolean isCopyMode() {
        return isValidCopyTemplate(inventory.getStackInSlot(TEMPLATE_SLOT));
    }

    public boolean isPaymentItem(ItemStack stack) {
        return EnchantmentTableRules.paymentCostFor(stack.getItem(), config()) > 0;
    }

    public boolean isValidCopyTemplate(ItemStack stack) {
        if (level == null || !stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        var enchantments = EnchantmentUtils.getEnchantments(stack);
        if (enchantments.size() != 1) {
            return false;
        }

        Object2IntMap.Entry<Holder<Enchantment>> entry = enchantments.entrySet().iterator().next();
        Holder<Enchantment> enchantment = EnchantmentUtils.resolveEnchantmentHolder(level, entry.getKey()).orElse(entry.getKey());
        int level = entry.getIntValue();
        return EnchantmentTableRules.isValidSingleEnchantmentTemplate(
                enchantments.size(),
                level,
                enchantment.value().getMaxLevel()
        );
    }

    public void refreshCopyResult() {
        updatingCopyResult = true;
        TableOperationResult result;
        try {
            result = ConversionTableSession.refreshCopyResult(
                    logicalInventory,
                    this::isCopyMode,
                    this::config,
                    BOOK_SLOT,
                    PAYMENT_SLOT,
                    TEMPLATE_SLOT,
                    COPY_RESULT_SLOT
            );
        } finally {
            updatingCopyResult = false;
        }

        if (result.changed()) {
            markInventoryChanged();
        }
    }

    public void dropInventory() {
        if (level == null
                //? if >=1.21.6 {
                || level.isClientSide()
                //?} else {
                /*|| level.isClientSide
                *///?}
        ) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy());
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    //? if >=1.21.6 {
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(inventory::deserialize);
        refreshCopyResult();
    }
    //?} else if >=1.21.5 {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            tag.getCompound("Inventory").ifPresent(inventoryTag -> inventory.deserializeNBT(registries, inventoryTag));
        }
        refreshCopyResult();
    }
    *///?} else {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        refreshCopyResult();
    }
    *///?}

    private void markInventoryChanged() {
        inventoryVersion++;
        setChanged();
    }

    private TableConfigView config() {
        return Config.snapshot();
    }

    //? if >=1.21.9 {
    private class ConversionAutomationItemHandler implements ResourceHandler<ItemResource>, AutomationPort {
        private final SnapshotJournal<ItemStack[]> snapshotJournal = new SnapshotJournal<>() {
            @Override
            protected ItemStack[] createSnapshot() {
                ItemStack[] snapshot = new ItemStack[SLOT_COUNT];
                for (int i = 0; i < SLOT_COUNT; i++) {
                    snapshot[i] = inventory.getStackInSlot(i).copy();
                }
                return snapshot;
            }

            @Override
            protected void revertToSnapshot(ItemStack[] snapshot) {
                for (int i = 0; i < SLOT_COUNT; i++) {
                    inventory.setStackInSlot(i, snapshot[i]);
                }
            }
        };

        @Override
        public int size() {
            return 3;
        }

        @Override
        public int getSlots() {
            return size();
        }

        @Override
        public ItemResource getResource(int index) {
            return index == 2 ? ItemResource.of(inventory.getStackInSlot(COPY_RESULT_SLOT)) : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return index == 2 ? inventory.getStackInSlot(COPY_RESULT_SLOT).getCount() : 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return switch (index) {
                case 0, 1 -> !resource.isEmpty() && isValid(index, resource) ? 64 : 0;
                case 2 -> resource.isEmpty() || resource.matches(inventory.getStackInSlot(COPY_RESULT_SLOT)) ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public SlotRole getRole(int slot) {
            return switch (slot) {
                case 0 -> SlotRole.BOOK_INPUT;
                case 1 -> SlotRole.PAYMENT;
                case 2 -> SlotRole.COPY_RESULT;
                default -> SlotRole.GENERATED_BOOK;
            };
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.BOOK);
                case 1 -> isPaymentItem(stack);
                default -> false;
            };
        }

        @Override
        public boolean canExtract(int slot) {
            return slot == 2;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) {
                return false;
            }
            return switch (index) {
                case 0 -> resource.is(Items.BOOK);
                case 1 -> isPaymentItem(resource.toStack());
                default -> false;
            };
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int internalSlot = switch (index) {
                case 0 -> BOOK_SLOT;
                case 1 -> PAYMENT_SLOT;
                default -> -1;
            };
            if (internalSlot < 0 || resource.isEmpty() || amount <= 0 || !isValid(index, resource)) {
                return 0;
            }
            snapshotJournal.updateSnapshots(transaction);
            ItemStack remainder = inventory.insertItem(internalSlot, resource.toStack(amount), false);
            if (remainder.getCount() != amount) {
                refreshCopyResult();
            }
            return amount - remainder.getCount();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (index != 2 || resource.isEmpty() || amount <= 0 || !resource.matches(inventory.getStackInSlot(COPY_RESULT_SLOT))) {
                return 0;
            }
            snapshotJournal.updateSnapshots(transaction);
            ItemStack extracted = inventory.extractItem(COPY_RESULT_SLOT, amount, false);
            if (!extracted.isEmpty()) {
                refreshCopyResult();
            }
            return extracted.getCount();
        }
    }
    //?} else {
    /*private class ConversionAutomationItemHandler implements IItemHandler, AutomationPort {
        @Override
        public int getSlots() {
            return 3;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 2 ? inventory.getStackInSlot(COPY_RESULT_SLOT) : ItemStack.EMPTY;
        }

        @Override
        public SlotRole getRole(int slot) {
            return switch (slot) {
                case 0 -> SlotRole.BOOK_INPUT;
                case 1 -> SlotRole.PAYMENT;
                case 2 -> SlotRole.COPY_RESULT;
                default -> SlotRole.GENERATED_BOOK;
            };
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack) {
            return isItemValid(slot, stack);
        }

        @Override
        public boolean canExtract(int slot) {
            return slot == 2;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int internalSlot = switch (slot) {
                case 0 -> BOOK_SLOT;
                case 1 -> PAYMENT_SLOT;
                default -> -1;
            };
            if (internalSlot < 0 || stack.isEmpty() || !inventory.isItemValid(internalSlot, stack)) {
                return stack;
            }
            ItemStack remainder = inventory.insertItem(internalSlot, stack, simulate);
            if (!simulate) {
                refreshCopyResult();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 2) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = inventory.extractItem(COPY_RESULT_SLOT, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                refreshCopyResult();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 2 ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.BOOK);
                case 1 -> isPaymentItem(stack);
                default -> false;
            };
        }
    }
    *///?}
}
