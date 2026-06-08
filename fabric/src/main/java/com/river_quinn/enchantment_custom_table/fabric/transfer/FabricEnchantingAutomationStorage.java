package com.river_quinn.enchantment_custom_table.fabric.transfer;

import com.river_quinn.enchantment_custom_table.core.inventory.AutomationPort;
import com.river_quinn.enchantment_custom_table.core.inventory.SlotRole;
import com.river_quinn.enchantment_custom_table.fabric.block.entity.FabricEnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.fabric.config.FabricTableConfig;
import com.river_quinn.enchantment_custom_table.fabric.screen.FabricEnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.fabric.util.FabricEnchantmentUtils;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class FabricEnchantingAutomationStorage extends SnapshotParticipant<ItemStack> implements InsertionOnlyStorage<ItemVariant>, AutomationPort {
    private final FabricEnchantingCustomTableBlockEntity blockEntity;

    public FabricEnchantingAutomationStorage(FabricEnchantingCustomTableBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (maxAmount <= 0 || !isValid(0, resource)) {
            return 0;
        }

        ItemStack enchantedBook = resource.toStack(1);
        EnchantmentTableRules.MergeResult result = mergeResult(enchantedBook);
        if (!result.allowed()) {
            return 0;
        }

        updateSnapshots(transaction);
        ItemStack toolStack = toolStack();
        EnchantmentHelper.setEnchantments(toolStack, result.enchantments());
        blockEntity.getInventory().setStackInSlot(FabricEnchantingCustomMenu.TOOL_SLOT, toolStack);
        playUseSound();
        return 1;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public SlotRole getRole(int slot) {
        return SlotRole.ENCHANTED_BOOK_INPUT;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack) {
        return slot == 0 && canApply(stack);
    }

    @Override
    public boolean canExtract(int slot) {
        return false;
    }

    @Override
    public long getVersion() {
        return blockEntity.getInventoryVersion();
    }

    private boolean isValid(int slot, ItemVariant resource) {
        return slot == 0 && !resource.isBlank() && canApply(resource.toStack(1));
    }

    private boolean canApply(ItemStack stack) {
        return hasValidInputs(stack) && mergeResult(stack).allowed();
    }

    private EnchantmentTableRules.MergeResult mergeResult(ItemStack enchantedBook) {
        Level level = blockEntity.getLevel();
        ItemStack toolStack = toolStack();
        return EnchantmentTableRules.tryMergeEnchantments(
                FabricEnchantmentUtils.getEnchantments(toolStack),
                FabricEnchantmentUtils.getEnchantmentLevels(level, enchantedBook),
                enchantment -> FabricEnchantmentUtils.getCoreEnchantmentKey(level, enchantment),
                EnchantmentTableRules.MergeOptions.from(FabricTableConfig.snapshot())
        );
    }

    private boolean hasValidInputs(ItemStack enchantedBook) {
        return blockEntity.getLevel() != null
                && !toolStack().isEmpty()
                && enchantedBook.is(Items.ENCHANTED_BOOK);
    }

    private ItemStack toolStack() {
        return blockEntity.getInventory().getStackInSlot(FabricEnchantingCustomMenu.TOOL_SLOT);
    }

    private void playUseSound() {
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @Override
    protected ItemStack createSnapshot() {
        return toolStack().copy();
    }

    @Override
    protected void readSnapshot(ItemStack snapshot) {
        blockEntity.getInventory().setStackInSlot(FabricEnchantingCustomMenu.TOOL_SLOT, snapshot);
    }
}
