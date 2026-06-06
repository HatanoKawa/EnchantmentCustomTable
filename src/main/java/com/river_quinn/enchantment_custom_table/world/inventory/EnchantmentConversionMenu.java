package com.river_quinn.enchantment_custom_table.world.inventory;

import com.mojang.datafixers.util.Pair;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.core.inventory.LogicalInventory;
import com.river_quinn.enchantment_custom_table.core.layout.TableMenuLayout;
import com.river_quinn.enchantment_custom_table.core.net.ConversionTableActions;
import com.river_quinn.enchantment_custom_table.core.session.ConversionTableSession;
import com.river_quinn.enchantment_custom_table.core.session.GeneratedSlotPage;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
//? if <1.21.9 {
/*import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
*///?}
//? if >=1.21.9 {
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
//?}

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentConversionMenu extends AbstractContainerMenu implements ConversionTableActions {
	public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
	public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
	public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
	public static final int ENCHANTED_BOOK_SLOT_START = 2;
	public static final int TEMPLATE_BOOK_SLOT = ENCHANTED_BOOK_SLOT_START + ENCHANTED_BOOK_SLOT_SIZE;
	public static final int COPY_RESULT_SLOT = TEMPLATE_BOOK_SLOT + 1;
	public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = COPY_RESULT_SLOT + 1;
	private static final int PLAYER_MAIN_INVENTORY_SIZE = 27;
	private static final int PLAYER_INVENTORY_START = ENCHANTMENT_CONVERSION_SLOT_SIZE;
	private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + PLAYER_MAIN_INVENTORY_SIZE;
	/**
	 * index 0: 书本槽
	 * index 1: 绿宝石槽
	 * index 2-29: 附魔书槽
	 * index 30: 待复制附魔书模板槽
	 * index 31: 复制结果槽
	 */
	//? if >=1.21.9 {
	private final ConversionMenuItemHandler itemHandler;
	//?} else {
	/*private final IItemHandlerModifiable itemHandler;
	*///?}
	private final ConversionTableSession session;

	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private boolean hasValidPosition = false;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	public EnchantmentConversionTableBlockEntity boundBlockEntity = null;
	private int lastInventoryVersion = -1;

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
			hasValidPosition = true;
		}
		if (pos != null) {
			if (this.world.getBlockEntity(pos) instanceof EnchantmentConversionTableBlockEntity blockEntity) {
				boundBlockEntity = blockEntity;
			}
		}
		//? if >=1.21.9 {
		this.itemHandler = new ConversionMenuItemHandler(boundBlockEntity);
		LogicalInventory logicalInventory = new ResourceHandlerLogicalInventory(itemHandler);
		//?} else {
		/*ConversionMenuInventory logicalInventory = new ConversionMenuInventory(boundBlockEntity);
		this.itemHandler = new LogicalInventoryItemHandler(logicalInventory);
		*///?}
		this.session = new ConversionTableSession(
				world,
				logicalInventory,
				EnchantmentUtils.service(),
				this::isCopyMode,
				this::config,
				0,
				1,
				ENCHANTED_BOOK_SLOT_START,
				ENCHANTED_BOOK_SLOT_SIZE
		);

		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return session.currentPage();
			}

			@Override
			public void set(int value) {
				session.setCurrentPage(value);
				syncPageState();
			}
		});
		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return session.totalPage();
			}

			@Override
			public void set(int value) {
				session.setTotalPage(value);
				syncPageState();
			}
		});

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, 0, TableMenuLayout.Conversion.BOOK_SLOT_X, TableMenuLayout.Conversion.BOOK_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, 0, TableMenuLayout.Conversion.BOOK_SLOT_X, TableMenuLayout.Conversion.BOOK_SLOT_Y) {
			*///?}
			@Override
			public boolean mayPlace(ItemStack stack) {
				return Items.BOOK == stack.getItem();
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				refreshEnchantedBookSlotsAfterInputsChanged();
			}

			@Override
			//? if >=1.21.11 {
			public Identifier getNoItemIcon() {
				return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
			//?} else if >=1.21.4 {
			/*public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
			*///?} else {
			/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
				);
			}
			*///?}
		});

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, 1, TableMenuLayout.Conversion.PAYMENT_SLOT_X, TableMenuLayout.Conversion.PAYMENT_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, 1, TableMenuLayout.Conversion.PAYMENT_SLOT_X, TableMenuLayout.Conversion.PAYMENT_SLOT_Y) {
			*///?}
			@Override
			public boolean mayPlace(ItemStack stack) {
				return boundBlockEntity != null
						? boundBlockEntity.isPaymentItem(stack)
						: EnchantmentTableRules.paymentCostFor(stack.getItem(), config()) > 0;
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				refreshEnchantedBookSlotsAfterInputsChanged();
			}

			@Override
			//? if >=1.21.11 {
			public Identifier getNoItemIcon() {
				return Identifier.withDefaultNamespace("container/slot/emerald");
			}
			//?} else if >=1.21.4 {
			/*public ResourceLocation getNoItemIcon() {
				return ResourceLocation.withDefaultNamespace("container/slot/emerald");
			}
			*///?} else {
			/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("minecraft:item/empty_slot_emerald")
				);
			}
			*///?}
		});

		int enchanted_book_index = 0;
		for (int row = 0; row < ENCHANTED_BOOK_SLOT_ROW_COUNT; row++) {
			int yPos = TableMenuLayout.Conversion.generatedSlotY(row);
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = TableMenuLayout.Conversion.generatedSlotX(col);
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					//? if >=1.21.9 {
					new ResourceHandlerSlot(itemHandler, itemHandler::set, final_enchanted_book_index + ENCHANTED_BOOK_SLOT_START, xPos, yPos) {
					//?} else {
					/*new SlotItemHandler(itemHandler, final_enchanted_book_index + ENCHANTED_BOOK_SLOT_START, xPos, yPos) {
					*///?}
						@Override
						public boolean mayPlace(ItemStack stack) {
							return false;
						}

						@Override
						//? if >=1.21.11 {
						public Identifier getNoItemIcon() {
							return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
						}
						//?} else if >=1.21.4 {
						/*public ResourceLocation getNoItemIcon() {
							return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
						}
						*///?} else {
						/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
							return Pair.of(
									InventoryMenu.BLOCK_ATLAS,
									ResourceLocation.tryParse("enchantment_custom_table:item/empty_slot_book")
							);
						}
						*///?}

						@Override
						public boolean mayPickup(Player player) {
							return canPickEnchantedBook();
						}

						@Override
						public void onTake(Player player, ItemStack stack) {
							if (!world.isClientSide()) {
								pickEnchantedBook();
								getItem();
							}
						}

					}
				));
				enchanted_book_index++;
			}
		}

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, TEMPLATE_BOOK_SLOT, TableMenuLayout.Conversion.TEMPLATE_SLOT_X, TableMenuLayout.Conversion.TEMPLATE_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, TEMPLATE_BOOK_SLOT, TableMenuLayout.Conversion.TEMPLATE_SLOT_X, TableMenuLayout.Conversion.TEMPLATE_SLOT_Y) {
			*///?}
			@Override
			public boolean mayPlace(ItemStack stack) {
				return boundBlockEntity != null && boundBlockEntity.isValidCopyTemplate(stack);
			}

			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				super.setByPlayer(newStack, oldStack);
				regenerateEnchantedBookSlot();
			}

			@Override
			//? if >=1.21.11 {
			public Identifier getNoItemIcon() {
				return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/copy_template_slot");
			}
			//?} else if >=1.21.4 {
			/*public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/copy_template_slot");
			}
			*///?} else {
			/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/copy_template_slot")
				);
			}
			*///?}
		});

		this.addSlot(
			//? if >=1.21.9 {
			new ResourceHandlerSlot(itemHandler, itemHandler::set, COPY_RESULT_SLOT, TableMenuLayout.Conversion.COPY_RESULT_SLOT_X, TableMenuLayout.Conversion.COPY_RESULT_SLOT_Y) {
			//?} else {
			/*new SlotItemHandler(itemHandler, COPY_RESULT_SLOT, TableMenuLayout.Conversion.COPY_RESULT_SLOT_X, TableMenuLayout.Conversion.COPY_RESULT_SLOT_Y) {
			*///?}
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				super.onTake(player, stack);
				if (boundBlockEntity != null) {
					boundBlockEntity.refreshCopyResult();
				}
			}

			@Override
			//? if >=1.21.11 {
			public Identifier getNoItemIcon() {
				return Identifier.fromNamespaceAndPath("enchantment_custom_table", "container/slot/output_slot");
			}
			//?} else if >=1.21.4 {
			/*public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/output_slot");
			}
			*///?} else {
			/*public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(
						InventoryMenu.BLOCK_ATLAS,
						ResourceLocation.tryParse("enchantment_custom_table:item/output_slot")
				);
			}
			*///?}
		});

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, TableMenuLayout.Conversion.PLAYER_INVENTORY_X + sj * TableMenuLayout.SLOT_SIZE, TableMenuLayout.Conversion.PLAYER_INVENTORY_Y + si * TableMenuLayout.SLOT_SIZE));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, TableMenuLayout.Conversion.PLAYER_INVENTORY_X + si * TableMenuLayout.SLOT_SIZE, TableMenuLayout.Conversion.PLAYER_INVENTORY_Y + 58));

		regenerateEnchantedBookSlot();
		if (boundBlockEntity != null) {
			lastInventoryVersion = boundBlockEntity.getInventoryVersion();
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();
		if (boundBlockEntity != null && lastInventoryVersion != boundBlockEntity.getInventoryVersion()) {
			lastInventoryVersion = boundBlockEntity.getInventoryVersion();
			refreshEnchantedBookSlotsAfterInputsChanged();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return hasValidPosition
				&& AbstractContainerMenu.stillValid(access, player, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot) this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < ENCHANTMENT_CONVERSION_SLOT_SIZE) {
				if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, this.slots.size(), true))
					return ItemStack.EMPTY;
				slot.onQuickCraft(itemstack1, itemstack);
			} else if (!this.moveItemStackTo(itemstack1, 0, ENCHANTMENT_CONVERSION_SLOT_SIZE, false)) {
				if (index < PLAYER_HOTBAR_START) {
					if (!this.moveItemStackTo(itemstack1, PLAYER_HOTBAR_START, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else {
					if (!this.moveItemStackTo(itemstack1, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false))
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

		if (index < 2 || index == TEMPLATE_BOOK_SLOT || index == COPY_RESULT_SLOT) {
			refreshEnchantedBookSlotsAfterInputsChanged();
		}

		return itemstack;
	}

	@Override
	protected boolean moveItemStackTo(ItemStack p_38904_, int p_38905_, int p_38906_, boolean p_38907_) {
		return MenuStackMover.moveItemStackTo(this.slots, p_38904_, p_38905_, p_38906_, p_38907_);
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
	}

	public void setSearchQuery(String query, String clientLanguage, List<String> matchedEnchantments) {
		session.setSearchQuery(query, clientLanguage, matchedEnchantments);
		syncPageState();
	}

	public String getSearchQuery() {
		return session.searchQuery();
	}

	public String getSearchClientLanguage() {
		return session.searchClientLanguage();
	}

	public int currentPage = 0;
	public int totalPage = 0;

	public void nextPage() {
		session.nextPage();
		syncPageState();
	}

	public void previousPage() {
		session.previousPage();
		syncPageState();
	}

	public void turnPage(int page) {
		session.turnPage(page);
		syncPageState();
	}

	public void resetPage() {
		session.resetPage();
		syncPageState();
	}

	public void clearEnchantedBookSlot() {
		session.clearGeneratedSlots();
		syncPageState();
	}

	public void genEnchantedBookSlot() {
		session.generateGeneratedSlots();
		syncPageState();
	}

	public void regenerateEnchantedBookSlot() {
		session.regenerateGeneratedSlots();
		syncPageState();
	}

	private void refreshEnchantedBookSlotsAfterInputsChanged() {
		session.refreshGeneratedSlotsAfterInputsChanged();
		syncPageState();
	}

	public boolean canPickEnchantedBook() {
		return session.canPickGeneratedBook();
	}

	public boolean pickEnchantedBook() {
		var result = session.pickGeneratedBook();
		syncPageState();
		return result.success();
	}

	private boolean isCopyMode() {
		return boundBlockEntity != null && boundBlockEntity.isCopyMode();
	}

	private TableConfigView config() {
		return Config.snapshot();
	}

	private void syncPageState() {
		GeneratedSlotPage page = session.page();
		currentPage = page.currentPage();
		totalPage = page.totalPage();
	}

	//? if >=1.21.9 {
	private static class ConversionMenuItemHandler extends MenuItemStackHandler {
		private final EnchantmentConversionTableBlockEntity blockEntity;
		private final MenuItemStackHandler fallbackPersistentHandler = new MenuItemStackHandler(EnchantmentConversionTableBlockEntity.SLOT_COUNT, 64);

		private ConversionMenuItemHandler(EnchantmentConversionTableBlockEntity blockEntity) {
			super(ENCHANTMENT_CONVERSION_SLOT_SIZE);
			this.blockEntity = blockEntity;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return isPersistentSlot(slot)
					? persistentHandler().getStackInSlot(toPersistentSlot(slot))
					: super.getStackInSlot(slot);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (isPersistentSlot(slot)) {
				persistentHandler().setStackInSlot(toPersistentSlot(slot), stack);
			} else {
				super.setStackInSlot(slot, stack);
			}
		}

		@Override
		public void set(int index, ItemResource resource, int amount) {
			if (isPersistentSlot(index)) {
				persistentHandler().setStackInSlot(toPersistentSlot(index), resource.toStack(amount));
			} else {
				super.set(index, resource, amount);
			}
		}

		@Override
		public ItemResource getResource(int index) {
			return isPersistentSlot(index) ? ItemResource.of(getStackInSlot(index)) : super.getResource(index);
		}

		@Override
		public long getAmountAsLong(int index) {
			return isPersistentSlot(index) ? getStackInSlot(index).getCount() : super.getAmountAsLong(index);
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			if (!isPersistentSlot(index)) {
				return super.isValid(index, resource);
			}
			if (resource.isEmpty()) {
				return false;
			}
			return persistentHandler().isItemValid(toPersistentSlot(index), resource.toStack());
		}

		@Override
		public long getCapacityAsLong(int index, ItemResource resource) {
			if (!isPersistentSlot(index)) {
				return super.getCapacityAsLong(index, resource);
			}
			int slotLimit = persistentHandler().getCapacityAsInt(toPersistentSlot(index), resource);
			return resource.isEmpty() ? slotLimit : Math.min(slotLimit, resource.toStack().getMaxStackSize());
		}

		@Override
		public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (!isPersistentSlot(index)) {
				return super.insert(index, resource, amount, transaction);
			}

			ItemStack current = getStackInSlot(index);
			if (resource.isEmpty() || (!current.isEmpty() && !resource.matches(current)) || !isValid(index, resource)) {
				return 0;
			}
			int capacity = (int) getCapacityAsLong(index, resource);
			return Math.min(amount, Math.max(0, capacity - current.getCount()));
		}

		@Override
		public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
			if (!isPersistentSlot(index)) {
				return super.extract(index, resource, amount, transaction);
			}

			ItemStack current = getStackInSlot(index);
			if (resource.isEmpty() || !resource.matches(current)) {
				return 0;
			}
			return Math.min(amount, current.getCount());
		}

		private MenuItemStackHandler persistentHandler() {
			return blockEntity != null ? blockEntity.getInventory() : fallbackPersistentHandler;
		}

		private boolean isPersistentSlot(int slot) {
			return slot < ENCHANTED_BOOK_SLOT_START || slot >= TEMPLATE_BOOK_SLOT;
		}

		private int toPersistentSlot(int slot) {
			return switch (slot) {
				case 0, 1 -> slot;
				case TEMPLATE_BOOK_SLOT -> EnchantmentConversionTableBlockEntity.TEMPLATE_SLOT;
				case COPY_RESULT_SLOT -> EnchantmentConversionTableBlockEntity.COPY_RESULT_SLOT;
				default -> throw new IndexOutOfBoundsException(slot);
			};
		}
	}
	//?} else {
	/*private static class ConversionMenuInventory implements LogicalInventory {
		private final EnchantmentConversionTableBlockEntity blockEntity;
		private final LogicalInventory fallbackPersistentInventory = new ItemHandlerLogicalInventory(new ItemStackHandler(EnchantmentConversionTableBlockEntity.SLOT_COUNT));
		private final LogicalInventory virtualInventory = new ItemHandlerLogicalInventory(new ItemStackHandler(ENCHANTED_BOOK_SLOT_SIZE));

		private ConversionMenuInventory(EnchantmentConversionTableBlockEntity blockEntity) {
			this.blockEntity = blockEntity;
		}

		@Override
		public int getSlots() {
			return ENCHANTMENT_CONVERSION_SLOT_SIZE;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				return persistentInventory().getStackInSlot(slot);
			}
			if (slot < TEMPLATE_BOOK_SLOT) {
				return virtualInventory.getStackInSlot(slot - ENCHANTED_BOOK_SLOT_START);
			}
			return persistentInventory().getStackInSlot(toPersistentSlot(slot));
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				persistentInventory().setStackInSlot(slot, stack);
			} else if (slot < TEMPLATE_BOOK_SLOT) {
				virtualInventory.setStackInSlot(slot - ENCHANTED_BOOK_SLOT_START, stack);
			} else {
				persistentInventory().setStackInSlot(toPersistentSlot(slot), stack);
			}
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				return persistentInventory().insertItem(slot, stack, simulate);
			}
			if (slot < TEMPLATE_BOOK_SLOT) {
				return virtualInventory.insertItem(slot - ENCHANTED_BOOK_SLOT_START, stack, simulate);
			}
			return persistentInventory().insertItem(toPersistentSlot(slot), stack, simulate);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				return persistentInventory().extractItem(slot, amount, simulate);
			}
			if (slot < TEMPLATE_BOOK_SLOT) {
				return virtualInventory.extractItem(slot - ENCHANTED_BOOK_SLOT_START, amount, simulate);
			}
			return persistentInventory().extractItem(toPersistentSlot(slot), amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				return persistentInventory().getSlotLimit(slot);
			}
			if (slot < TEMPLATE_BOOK_SLOT) {
				return virtualInventory.getSlotLimit(slot - ENCHANTED_BOOK_SLOT_START);
			}
			return persistentInventory().getSlotLimit(toPersistentSlot(slot));
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot < ENCHANTED_BOOK_SLOT_START) {
				return persistentInventory().isItemValid(slot, stack);
			}
			if (slot < TEMPLATE_BOOK_SLOT) {
				return false;
			}
			return persistentInventory().isItemValid(toPersistentSlot(slot), stack);
		}

		private LogicalInventory persistentInventory() {
			return blockEntity != null ? blockEntity.getLogicalInventory() : fallbackPersistentInventory;
		}

		private int toPersistentSlot(int slot) {
			return slot == TEMPLATE_BOOK_SLOT
					? EnchantmentConversionTableBlockEntity.TEMPLATE_SLOT
					: EnchantmentConversionTableBlockEntity.COPY_RESULT_SLOT;
		}
	}
	*///?}
}
