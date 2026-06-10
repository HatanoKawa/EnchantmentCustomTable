package com.river_quinn.enchantment_custom_table.block.entity;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.core.access.EnchantmentList;
import com.river_quinn.enchantment_custom_table.core.inventory.AutomationPort;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.inventory.SlotRole;
import com.river_quinn.enchantment_custom_table.init.ModBlockEntities;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.ItemHandlerLogicalInventory;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnchantingCustomTableBlockEntity extends EnchantingTableLikeBlockEntity implements MenuProvider {
    public static final int TOOL_SLOT = 0;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            markInventoryChanged();
        }
    };
    private final LogicalInventory logicalInventory = new ItemHandlerLogicalInventory(inventory);
    private final IItemHandler automationHandler = new EnchantingAutomationItemHandler();
    private final LazyOptional<IItemHandler> automationCapability = LazyOptional.of(() -> automationHandler);
    private int inventoryVersion = 0;

    public EnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(), pos, state);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EnchantingCustomMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    public BlockPos getWorldPosition() {
        return this.worldPosition;
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

    public ItemStack getToolStack() {
        return inventory.getStackInSlot(TOOL_SLOT);
    }

    public void setToolStack(ItemStack stack) {
        inventory.setStackInSlot(TOOL_SLOT, stack);
    }

    public boolean replaceToolEnchantments(EnchantmentList enchantments) {
        ItemStack toolStack = getToolStack();
        if (toolStack.isEmpty()) {
            return false;
        }
        EnchantmentUtils.setEnchantments(toolStack, enchantments);
        markInventoryChanged();
        return true;
    }

    public boolean canApplyEnchantedBook(ItemStack stack, EnchantmentTableRules.MergeOptions mergeOptions) {
        if (!stack.is(Items.ENCHANTED_BOOK) || getToolStack().isEmpty() || level == null) {
            return false;
        }
        return EnchantmentTableRules.tryMergeEnchantments(
                EnchantmentUtils.getEnchantments(getToolStack()),
                EnchantmentUtils.getEnchantmentLevels(level, stack),
                mergeOptions
        ).allowed();
    }

    public boolean tryApplyEnchantedBook(ItemStack stack, EnchantmentTableRules.MergeOptions mergeOptions, boolean simulate) {
        if (!stack.is(Items.ENCHANTED_BOOK) || getToolStack().isEmpty() || level == null) {
            return false;
        }

        List<EnchantmentTableRules.EnchantmentLevel> enchantmentLevels = EnchantmentUtils.getEnchantmentLevels(level, stack);
        if (enchantmentLevels.isEmpty()) {
            return false;
        }

        ItemStack toolStack = getToolStack();
        EnchantmentTableRules.MergeResult result = EnchantmentTableRules.tryMergeEnchantments(
                EnchantmentUtils.getEnchantments(toolStack),
                enchantmentLevels,
                mergeOptions
        );
        if (!result.allowed()) {
            return false;
        }

        if (!simulate) {
            replaceToolEnchantments(result.enchantments());
            playUseSound();
        }
        return true;
    }

    public void dropInventory() {
        if (level == null || level.isClientSide) {
            return;
        }
        ItemStack stack = getToolStack();
        if (!stack.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy());
            inventory.setStackInSlot(TOOL_SLOT, ItemStack.EMPTY);
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
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return automationCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automationCapability.invalidate();
    }

    private EnchantmentTableRules.MergeOptions mergeOptions() {
        return EnchantmentTableRules.MergeOptions.from(Config.snapshot());
    }

    private void playUseSound() {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void markInventoryChanged() {
        inventoryVersion++;
        setChanged();
    }

    private class EnchantingAutomationItemHandler implements IItemHandler, AutomationPort {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public SlotRole getRole(int slot) {
            return SlotRole.ENCHANTED_BOOK_INPUT;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack) {
            return isItemValid(slot, stack);
        }

        @Override
        public boolean canExtract(int slot) {
            return false;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !canApplyEnchantedBook(stack, mergeOptions())) {
                return stack;
            }
            if (!simulate) {
                tryApplyEnchantedBook(copyWithCount(stack, 1), mergeOptions(), false);
            }
            return stack.getCount() == 1 ? ItemStack.EMPTY : copyWithCount(stack, stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && canApplyEnchantedBook(stack, mergeOptions());
        }
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }
}
