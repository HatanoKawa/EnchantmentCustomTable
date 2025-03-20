package com.river_quinn.enchantment_custom_table.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingCustomTableBlockEntity extends BlockEntity implements Nameable {
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
    private static final RandomSource RANDOM = RandomSource.create();
    @Nullable
    private Component name;

    public EnchantingCustomTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENCHANTING_CUSTOM_TABLE.get(), pos, state);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Serializer.toJson(this.name, registries));
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(tag.getString("CustomName"), registries);
        }
    }

    public static void bookAnimationTick(Level level, BlockPos pos, BlockState state, EnchantingCustomTableBlockEntity enchantingTable) {
        enchantingTable.oOpen = enchantingTable.open;
        enchantingTable.oRot = enchantingTable.rot;
        Player player = level.getNearestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, (double)3.0F, false);
        if (player != null) {
            double d0 = player.getX() - ((double)pos.getX() + (double)0.5F);
            double d1 = player.getZ() - ((double)pos.getZ() + (double)0.5F);
            enchantingTable.tRot = (float)Mth.atan2(d1, d0);
            enchantingTable.open += 0.1F;
            if (enchantingTable.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = enchantingTable.flipT;

                do {
                    enchantingTable.flipT += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while(f1 == enchantingTable.flipT);
            }
        } else {
            enchantingTable.tRot += 0.02F;
            enchantingTable.open -= 0.1F;
        }

        while(enchantingTable.rot >= (float)Math.PI) {
            enchantingTable.rot -= ((float)Math.PI * 2F);
        }

        while(enchantingTable.rot < -(float)Math.PI) {
            enchantingTable.rot += ((float)Math.PI * 2F);
        }

        while(enchantingTable.tRot >= (float)Math.PI) {
            enchantingTable.tRot -= ((float)Math.PI * 2F);
        }

        while(enchantingTable.tRot < -(float)Math.PI) {
            enchantingTable.tRot += ((float)Math.PI * 2F);
        }

        float f2;
        for(f2 = enchantingTable.tRot - enchantingTable.rot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
        }

        while(f2 < -(float)Math.PI) {
            f2 += ((float)Math.PI * 2F);
        }

        enchantingTable.rot += f2 * 0.4F;
        enchantingTable.open = Mth.clamp(enchantingTable.open, 0.0F, 1.0F);
        ++enchantingTable.time;
        enchantingTable.oFlip = enchantingTable.flip;
        float f = (enchantingTable.flipT - enchantingTable.flip) * 0.4F;
        float f3 = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        enchantingTable.flipA += (f - enchantingTable.flipA) * 0.9F;
        enchantingTable.flip += enchantingTable.flipA;
    }

    public Component getName() {
        return (Component)(this.name != null ? this.name : Component.translatable("container.enchant"));
    }

    public void setCustomName(@Nullable Component customName) {
        this.name = customName;
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }
}
