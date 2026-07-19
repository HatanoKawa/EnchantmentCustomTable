package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentEntry;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
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
import com.river_quinn.enchantment_custom_table.world.inventory.ItemHandlerLogicalInventory;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantmentConversionTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int BOOK_SLOT = 0;
    public static final int PAYMENT_SLOT = 1;
    public static final int TEMPLATE_SLOT = 2;
    public static final int COPY_RESULT_SLOT = 3;
    public static final int SLOT_COUNT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case BOOK_SLOT -> EnchantmentTableRules.acceptsConversionBookInput(config()) && stack.is(Items.BOOK);
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
    private final IItemHandler automationHandler = new ConversionAutomationItemHandler();
    private final LazyOptional<IItemHandler> automationCapability = LazyOptional.of(() -> automationHandler);
    private boolean updatingCopyResult = false;
    private int inventoryVersion = 0;

    public EnchantmentConversionTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTMENT_CONVERSION_TABLE.get(), pos, state);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EnchantmentConversionMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public LogicalInventory getLogicalInventory() {
        return logicalInventory;
    }

    public int getInventoryVersion() {
        return inventoryVersion;
    }

    public @Nullable IItemHandler getAutomationItemHandler(@Nullable Direction direction) {
        return automationHandler;
    }

    public boolean isCopyMode() {
        return isValidCopyTemplate(inventory.getStackInSlot(TEMPLATE_SLOT));
    }

    public boolean isPaymentItem(ItemStack stack) {
        return EnchantmentTableRules.isConversionPaymentItem(stack, config());
    }

    public boolean isValidCopyTemplate(ItemStack stack) {
        if (level == null || !stack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        EnchantmentList enchantments = EnchantmentUtils.getEnchantments(stack);
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
        if (level == null || level.isClientSide) {
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        refreshCopyResult();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return automationCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automationCapability.invalidate();
    }

    private void markInventoryChanged() {
        inventoryVersion++;
        setChanged();
    }

    private TableConfigView config() {
        return Config.snapshot();
    }

    private class ConversionAutomationItemHandler implements IItemHandler, AutomationPort {
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
                case 0 -> EnchantmentTableRules.acceptsConversionBookInput(config()) && stack.is(Items.BOOK);
                case 1 -> isPaymentItem(stack);
                default -> false;
            };
        }
    }
}
