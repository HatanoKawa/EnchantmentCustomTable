package com.river_quinn.enchantment_custom_table.fabric.screen;

import com.river_quinn.enchantment_custom_table.fabric.init.FabricModBlocks;
import com.river_quinn.enchantment_custom_table.fabric.init.FabricModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FabricEnchantingCustomMenu extends AbstractContainerMenu {
    public final Level world;
    public final Player entity;
    public final int x;
    public final int y;
    public final int z;
    private final ContainerLevelAccess access;

    public FabricEnchantingCustomMenu(int id, Inventory inventory, BlockPos pos) {
        super(FabricModMenus.ENCHANTING_CUSTOM, id);
        this.entity = inventory.player;
        this.world = inventory.player.level();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.access = ContainerLevelAccess.create(world, pos);
        addPlayerInventory(inventory, 8, 84);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, FabricModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK);
    }

    private void addPlayerInventory(Inventory inventory, int xOffset, int yOffset) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, xOffset + column * 18, yOffset + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, xOffset + column * 18, yOffset + 58));
        }
    }
}
