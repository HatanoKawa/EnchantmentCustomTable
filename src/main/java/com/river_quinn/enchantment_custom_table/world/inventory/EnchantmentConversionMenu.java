package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EnchantmentConversionMenu extends AbstractContainerMenu {
	public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
	public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
	public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
	public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = ENCHANTED_BOOK_SLOT_SIZE + 2;
	/**
	 * index 0: 书本槽
	 * index 1: 绿宝石槽
	 * index 2-29: 附魔书槽
	 */
	private final ItemStackHandler itemHandler = new ItemStackHandler(ENCHANTMENT_CONVERSION_SLOT_SIZE);

	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	private boolean bound = false;
	private Supplier<Boolean> boundItemMatcher = null;
	private Entity boundEntity = null;
	public EnchantmentConversionTableBlockEntity boundBlockEntity = null;

	public EnchantmentConversionMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(ModMenus.ENCHANTMENT_CONVERSION.get(), id);
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
			boundBlockEntity = (EnchantmentConversionTableBlockEntity) this.world.getBlockEntity(pos);
		}

		this.addSlot(new SlotItemHandler(itemHandler, 0, 16, 8) {
			private final int slot = 0;
			private int x = EnchantmentConversionMenu.this.x;
			private int y = EnchantmentConversionMenu.this.y;

			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.BOOK == stack.getItem();
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty() && !oldStack.isEmpty()) {
					genEnchantedBookSlot();
				} else {
					regenerateEnchantedBookSlot();
				}
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
		});

		this.addSlot(new SlotItemHandler(itemHandler, 1, 16, 26) {
			private final int slot = 1;
			private int x = EnchantmentConversionMenu.this.x;
			private int y = EnchantmentConversionMenu.this.y;

			@Override
			public boolean mayPlace(ItemStack stack) {
				return (Items.EMERALD == stack.getItem() && Config.minimumEmeraldCost > 0)
						|| (Items.EMERALD_BLOCK == stack.getItem() && Config.minimumEmeraldBlockCost > 0);
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				if (!newStack.isEmpty() && !oldStack.isEmpty()) {
					genEnchantedBookSlot();
				} else {
					regenerateEnchantedBookSlot();
				}
			}

			@Override
			public ResourceLocation getNoItemIcon() {
				return ResourceLocation.withDefaultNamespace("container/slot/emerald");
			}
		});

		int enchanted_book_index = 0;
		for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
			int yPos = 8 + row * 18;
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = 43 + col * 18;
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					new SlotItemHandler(itemHandler, final_enchanted_book_index + 2, xPos, yPos) {
						private final int slot = final_enchanted_book_index + 2;
						private int x = EnchantmentConversionMenu.this.x;
						private int y = EnchantmentConversionMenu.this.y;

						@Override
						public boolean mayPlace(ItemStack stack) {
							return false;
						}

						@Override
						public ResourceLocation getNoItemIcon() {
							return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
						}

						@Override
						public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
							super.setByPlayer(newStack, oldStack);
							pickEnchantedBook();
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
			if (index < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
				if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CONVERSION_SLOT_SIZE, false)) {
				if (index < ENCHANTMENT_CONVERSION_SLOT_SIZE + 27) {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE + 27, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, ENCHANTMENT_CONVERSION_SLOT_SIZE, ENCHANTMENT_CONVERSION_SLOT_SIZE + 27, false))
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

		if (index < 2) {
			genEnchantedBookSlot();
		} else if (index < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
			pickEnchantedBook();
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
			playerIn.getInventory().placeItemBackInInventory(itemHandler.getStackInSlot(0));
			playerIn.getInventory().placeItemBackInInventory(itemHandler.getStackInSlot(1));
		}
	}

	public static final List<Integer> allEnchantments = new ArrayList<>();

	public void tryGetAllEnchantments() {
		if (allEnchantments.isEmpty()) {
			Registry<Enchantment> fullEnchantmentList = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			IdMap<Holder<Enchantment>> allRegisteredEnchantments = fullEnchantmentList.asHolderIdMap();
			allRegisteredEnchantments.forEach(enchantment ->
					allEnchantments.add(fullEnchantmentList.getId(enchantment.value())));
		}
	}

	public ItemStack getEnchantedBook(int enchantmentId) {
		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

		Enchantment enchantment = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).byId(enchantmentId);
		int enchantmentLevel = enchantment.getMaxLevel();
		var enchantmentReference = EnchantmentUtils.translateEnchantment(world, enchantment);
		assert enchantmentReference != null;
		enchantedBook.enchant(enchantmentReference, enchantmentLevel);

		return enchantedBook;
	}

	public int currentPage = 0;
	public int totalPage = 0;

	public void nextPage() {
		if (currentPage < (totalPage - 1)) {
			turnPage(currentPage + 1);
		}
	}

	public void previousPage() {
		if (currentPage > 0) {
			turnPage(currentPage - 1);
		}
	}

	public void turnPage(int page) {
		currentPage = page;
		clearEnchantedBookSlot();
		genEnchantedBookSlot();
	}

	public void resetPage() {
		currentPage = 0;
		totalPage = 0;
	}

	public void clearEnchantedBookSlot() {
		for (int i = 2; i < ENCHANTMENT_CONVERSION_SLOT_SIZE; i++) {
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	public void genEnchantedBookSlot() {
		tryGetAllEnchantments();
		boolean hasBook = itemHandler.getStackInSlot(0).is(Items.BOOK);
		boolean hasEnoughEmerald = false;
		if (itemHandler.getStackInSlot(1).is(Items.EMERALD) && Config.minimumEmeraldCost > 0) {
			hasEnoughEmerald = itemHandler.getStackInSlot(1).getCount() >= Config.minimumEmeraldCost;
		} else if (itemHandler.getStackInSlot(1).is(Items.EMERALD_BLOCK) && Config.minimumEmeraldBlockCost > 0) {
			hasEnoughEmerald = itemHandler.getStackInSlot(1).getCount() >= Config.minimumEmeraldBlockCost;
		}

		if (!hasBook || !hasEnoughEmerald) {
			resetPage();
			clearEnchantedBookSlot();
			return;
		}

		for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
			int slotIndex = i + 2;
			int enchantmentIndex = i + currentPage * ENCHANTED_BOOK_SLOT_SIZE;

			if (enchantmentIndex < allEnchantments.size()) {
				if (itemHandler.getStackInSlot(slotIndex).isEmpty()) {
					int enchantmentId = allEnchantments.get(enchantmentIndex);
					itemHandler.setStackInSlot(slotIndex, getEnchantedBook(enchantmentId));
				}
			} else {
				itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
			}
		}
	}

	public void regenerateEnchantedBookSlot() {
		currentPage = 0;
		totalPage = (int) Math.ceil(allEnchantments.size() / (double) ENCHANTED_BOOK_SLOT_SIZE);
		genEnchantedBookSlot();
	}

	public void pickEnchantedBook() {
		itemHandler.getStackInSlot(0).shrink(1);
		if (itemHandler.getStackInSlot(1).is(Items.EMERALD))
			itemHandler.getStackInSlot(1).shrink(Config.minimumEmeraldCost);
		else if (itemHandler.getStackInSlot(1).is(Items.EMERALD_BLOCK))
			itemHandler.getStackInSlot(1).shrink(Config.minimumEmeraldBlockCost);
		genEnchantedBookSlot();
	}
}
