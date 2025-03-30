
package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

import static com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity.*;

public class EnchantingCustomMenu extends AbstractContainerMenu {
	private static final Logger LOGGER = LogUtils.getLogger();
	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private ItemStackHandler internal;
	private Slot enchantableItemSlot;
	private Slot enchantedBookToAppendSlot;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	private boolean bound = false;
	private Supplier<Boolean> boundItemMatcher = null;
	private Entity boundEntity = null;
	public EnchantingCustomTableBlockEntity boundBlockEntity = null;
	private ItemStackHandler boundInv = null;

	public EnchantingCustomMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(ModMenus.ENCHANTING_CUSTOM.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();

		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
		if (pos != null) {
			boundBlockEntity = (EnchantingCustomTableBlockEntity)this.world.getBlockEntity(pos);
			if (boundBlockEntity instanceof EnchantingCustomTableBlockEntity baseContainerBlockEntity) {
				this.internal = baseContainerBlockEntity.getItemHandler();
//				this.bound = true;
				this.boundInv = boundBlockEntity.getItemHandler();
			}
		}

		this.enchantableItemSlot = this.addSlot(new SlotItemHandler(internal, 0, 8, 8) {
			private final int slot = 0;
			private int x = EnchantingCustomMenu.this.x;
			private int y = EnchantingCustomMenu.this.y;

			@Override
			public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
				super.onQuickCraft(newStack, oldStack);
				boundBlockEntity.clearEnchantedBookStore();
				boundBlockEntity.resetPage();
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty()) {
					// 放置待附魔工具，重新生成附魔书槽
					boundBlockEntity.genEnchantedBookStore();
				} else {
					// 取出待附魔工具，清空附魔书槽
					boundBlockEntity.clearEnchantedBookStore();
					boundBlockEntity.resetPage();
				}
			}
		});

		this.enchantedBookToAppendSlot = this.addSlot(new SlotItemHandler(internal, 1, 42, 8) {
			private final int slot = 1;
			private int x = EnchantingCustomMenu.this.x;
			private int y = EnchantingCustomMenu.this.y;

			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.ENCHANTED_BOOK == stack.getItem() && !getItemHandler().getStackInSlot(0).isEmpty();
			}

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
				);
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty()) {
					// 放置附魔书，同步添加工具上的附魔，并删除附加槽的附魔书，重新生成附魔书槽
					boundBlockEntity.addEnchantment(getEnchantmentInstanceFromEnchantedBook(newStack), true);
				} else {
					// 合法情况下不应该存在这种状况
					LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
				}
			}
		});

		int enchanted_book_index = 0;
		for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
			int yPos = 8 + row * 18;
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = 61 + col * 18;
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					new SlotItemHandler(internal, final_enchanted_book_index + 2, xPos, yPos) {
						private final int slot = final_enchanted_book_index + 2;
						private int x = EnchantingCustomMenu.this.x;
						private int y = EnchantingCustomMenu.this.y;

						@Override
						public boolean mayPlace(ItemStack stack) {
							return Items.ENCHANTED_BOOK == stack.getItem() && !getItemHandler().getStackInSlot(0).isEmpty();
						}

						@Override
						public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(
									InventoryMenu.BLOCK_ATLAS,
									ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
							);
						}

						@Override
						public void onQuickCraft(ItemStack newStack, ItemStack oldStack) {
							super.onQuickCraft(newStack, oldStack);
							if (!newStack.isEmpty() && oldStack.isEmpty()) {
								// 合法情况下不应该存在这种状况
								LOGGER.warn("stack 1, setByPlayer() called with newStack.isEmpty()");
							} else if (newStack.isEmpty() && !oldStack.isEmpty()) {
								// 移除附魔书，同步移除工具上的附魔
								boundBlockEntity.removeEnchantment(getEnchantmentInstanceFromEnchantedBook(oldStack));
							}
						}

						@Override
						public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
							super.setByPlayer(newStack, oldStack);
							if (!newStack.isEmpty() && oldStack.isEmpty()) {
								// 放置附魔书，同步添加工具上的附魔
								boundBlockEntity.addEnchantment(getEnchantmentInstanceFromEnchantedBook(newStack));
							} else if (newStack.isEmpty() && !oldStack.isEmpty()) {
								// 移除附魔书，同步移除工具上的附魔
								boundBlockEntity.removeEnchantment(getEnchantmentInstanceFromEnchantedBook(oldStack));
							}
						}

					}
				));
				enchanted_book_index++;
			}
		}

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142));

		this.boundBlockEntity.initMenu();
	}

	@Override
	public boolean stillValid(Player player) {
//		if (this.bound) {
//			if (this.boundItemMatcher != null)
//				return this.boundItemMatcher.get();
//			else if (this.boundBlockEntity != null)
//				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
//			else if (this.boundEntity != null)
//				return this.boundEntity.isAlive();
//		}
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE) {
				if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, false)) {
				if (index < ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27) {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE, ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27, false))
						return ItemStack.EMPTY;
				}
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			if (itemstack1.getCount() == itemstack.getCount())
				return ItemStack.EMPTY;
			slot.onTake(playerIn, itemstack1);
		}
		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		boolean flag = false;
		int i = p_38905_;
		if (p_38907_) {
			i = p_38906_ - 1;
		}
		if (p_38904_.isStackable()) {
			while (!p_38904_.isEmpty() && (p_38907_ ? i >= p_38905_ : i < p_38906_)) {
				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if (slot.mayPlace(itemstack) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
					int j = itemstack.getCount() + p_38904_.getCount();
					int k = slot.getMaxStackSize(itemstack);
					if (j <= k) {
						p_38904_.setCount(0);
						itemstack.setCount(j);
						slot.set(itemstack);
						flag = true;
					} else if (itemstack.getCount() < k) {
						p_38904_.shrink(k - itemstack.getCount());
						itemstack.setCount(k);
						slot.set(itemstack);
						flag = true;
					}
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		if (!p_38904_.isEmpty()) {
			if (p_38907_) {
				i = p_38906_ - 1;
			} else {
				i = p_38905_;
			}
			while (p_38907_ ? i >= p_38905_ : i < p_38906_) {
				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				if (itemstack1.isEmpty() && slot1.mayPlace(p_38904_)) {
					int l = slot1.getMaxStackSize(p_38904_);
					slot1.setByPlayer(p_38904_.split(Math.min(p_38904_.getCount(), l)));
					slot1.setChanged();
					flag = true;
					break;
				}
				if (p_38907_) {
					i--;
				} else {
					i++;
				}
			}
		}
		return flag;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!bound && playerIn instanceof ServerPlayer) {
//		if (!bound && playerIn instanceof ServerPlayer serverPlayer) {
//			if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
//				for (int j = 0; j < internal.getSlots(); ++j) {
//					playerIn.drop(internal.getStackInSlot(j), false);
//					if (internal instanceof IItemHandlerModifiable ihm)
//						ihm.setStackInSlot(j, ItemStack.EMPTY);
//				}
//			} else {
//				for (int i = 0; i < internal.getSlots(); ++i) {
//					playerIn.getInventory().placeItemBackInInventory(internal.getStackInSlot(i));
//					if (internal instanceof IItemHandlerModifiable ihm)
//						ihm.setStackInSlot(i, ItemStack.EMPTY);
//				}
//			}
			playerIn.getInventory().placeItemBackInInventory(internal.getStackInSlot(0));
			for (int i = 0; i < internal.getSlots(); ++i) {
				if (internal instanceof IItemHandlerModifiable ihm)
					ihm.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	public List<EnchantmentInstance> getEnchantmentInstanceFromEnchantedBook(ItemStack enchantedBookItemStack) {

		DataComponentType<ItemEnchantments> componentType = EnchantmentHelper.getComponentType(enchantedBookItemStack);
		var componentMap = enchantedBookItemStack.getComponents().get(componentType);

		List<EnchantmentInstance> enchantmentOfBook = new ArrayList<>();
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : componentMap.entrySet()) {
			Enchantment enchantment = entry.getKey().value();
			int enchantmentLevel = entry.getIntValue();
			enchantmentOfBook.add(new EnchantmentInstance(Holder.direct(enchantment), enchantmentLevel));
		}

		return enchantmentOfBook;
	}
}
