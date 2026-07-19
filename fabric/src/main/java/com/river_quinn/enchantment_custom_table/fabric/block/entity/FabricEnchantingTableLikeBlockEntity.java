package com.river_quinn.enchantment_custom_table.fabric.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public abstract class FabricEnchantingTableLikeBlockEntity extends BlockEntity {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final Random RANDOM = new Random();

    protected FabricEnchantingTableLikeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, FabricEnchantingTableLikeBlockEntity enchantingTable) {
        enchantingTable.oOpen = enchantingTable.open;
        enchantingTable.oRot = enchantingTable.rot;
        Player player = level.getNearestPlayer(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 3.0F, false);
        if (player != null) {
            double dx = player.getX() - (pos.getX() + 0.5F);
            double dz = player.getZ() - (pos.getZ() + 0.5F);
            enchantingTable.tRot = (float) Mth.atan2(dz, dx);
            enchantingTable.open += 0.1F;
            if (enchantingTable.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float previousFlip = enchantingTable.flipT;
                do {
                    enchantingTable.flipT += RANDOM.nextInt(4) - RANDOM.nextInt(4);
                } while (previousFlip == enchantingTable.flipT);
            }
        } else {
            enchantingTable.tRot += 0.02F;
            enchantingTable.open -= 0.1F;
        }

        while (enchantingTable.rot >= Math.PI) {
            enchantingTable.rot -= Math.PI * 2F;
        }
        while (enchantingTable.rot < -Math.PI) {
            enchantingTable.rot += Math.PI * 2F;
        }
        while (enchantingTable.tRot >= Math.PI) {
            enchantingTable.tRot -= Math.PI * 2F;
        }
        while (enchantingTable.tRot < -Math.PI) {
            enchantingTable.tRot += Math.PI * 2F;
        }

        float rotationDelta;
        for (rotationDelta = enchantingTable.tRot - enchantingTable.rot; rotationDelta >= Math.PI; rotationDelta -= Math.PI * 2F) {
        }
        while (rotationDelta < -Math.PI) {
            rotationDelta += Math.PI * 2F;
        }

        enchantingTable.rot += rotationDelta * 0.4F;
        enchantingTable.open = Mth.clamp(enchantingTable.open, 0.0F, 1.0F);
        enchantingTable.time++;
        enchantingTable.oFlip = enchantingTable.flip;
        float flipDelta = (enchantingTable.flipT - enchantingTable.flip) * 0.4F;
        flipDelta = Mth.clamp(flipDelta, -0.2F, 0.2F);
        enchantingTable.flipA += (flipDelta - enchantingTable.flipA) * 0.9F;
        enchantingTable.flip += enchantingTable.flipA;
    }
}
