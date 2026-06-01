package com.river_quinn.enchantment_custom_table.world.inventory;

import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.core.config.TableConfigView;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.init.ModMenus;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentSearchRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentTableRules;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EnchantmentConversionMenu extends AbstractContainerMenu {
	public static final int ENCHANTED_BOOK_SLOT_ROW_COUNT = 4;
	public static final int ENCHANTED_BOOK_SLOT_COLUMN_COUNT = 7;
	public static final int ENCHANTED_BOOK_SLOT_SIZE = ENCHANTED_BOOK_SLOT_ROW_COUNT * ENCHANTED_BOOK_SLOT_COLUMN_COUNT;
	public static final int ENCHANTED_BOOK_SLOT_START = 2;
	public static final int TEMPLATE_BOOK_SLOT = ENCHANTED_BOOK_SLOT_START + ENCHANTED_BOOK_SLOT_SIZE;
	public static final int COPY_RESULT_SLOT = TEMPLATE_BOOK_SLOT + 1;
	public static final int ENCHANTMENT_CONVERSION_SLOT_SIZE = COPY_RESULT_SLOT + 1;
	/**
	 * index 0: 书本槽
	 * index 1: 绿宝石槽
	 * index 2-29: 附魔书槽
	 * index 30: 待复制附魔书模板槽
	 * index 31: 复制结果槽
	 */
	private final ConversionMenuItemHandler itemHandler;

	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private boolean hasValidPosition = false;
	private final Map<Integer, Slot> enchantedBookSlots = new HashMap<>();
	public EnchantmentConversionTableBlockEntity boundBlockEntity = null;
	private int lastInventoryVersion = -1;
	private String searchQuery = "";
	private String searchClientLanguage = "";
	private boolean usingClientSearchMatches = false;
	private Set<ResourceLocation> clientMatchedEnchantments = Set.of();

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
		this.itemHandler = new ConversionMenuItemHandler(boundBlockEntity);

		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return currentPage;
			}

			@Override
			public void set(int value) {
				currentPage = value;
			}
		});
		this.addDataSlot(new DataSlot() {
			@Override
			public int get() {
				return totalPage;
			}

			@Override
			public void set(int value) {
				totalPage = value;
			}
		});

		this.addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, 0, 16, 8 + 15) {
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

		this.addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, 1, 16, 26 + 15) {
			private final int slot = 1;
			private int x = EnchantmentConversionMenu.this.x;
			private int y = EnchantmentConversionMenu.this.y;

			@Override
			public boolean mayPlace(ItemStack stack) {
				return boundBlockEntity != null
						? boundBlockEntity.isPaymentItem(stack)
						: EnchantmentTableRules.paymentCostFor(stack.getItem(), config()) > 0;
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
			int yPos = 8 + row * 18 + 15;
			for (int col = 0; col < ENCHANTED_BOOK_SLOT_COLUMN_COUNT; col++) {
				int xPos = 43 + col * 18;
				int final_enchanted_book_index = enchanted_book_index;
				this.enchantedBookSlots.put(final_enchanted_book_index, this.addSlot(
					new ResourceHandlerSlot(itemHandler, itemHandler::set, final_enchanted_book_index + ENCHANTED_BOOK_SLOT_START, xPos, yPos) {
						private final int slot = final_enchanted_book_index + ENCHANTED_BOOK_SLOT_START;
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
						public boolean mayPickup(Player player) {
							return canPickEnchantedBook();
						}

						@Override
						public void onTake(Player player, ItemStack stack) {
							if (!world.isClientSide()) {
								pickEnchantedBook();
								// Vanilla calls setChanged after onTake; refresh StackCopySlot's cache so it keeps the rebuilt result.
								getItem();
							}
						}

					}
				));
				enchanted_book_index++;
			}
		}

		this.addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, TEMPLATE_BOOK_SLOT, 16, 8 + 51) {
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
			public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
		});

		this.addSlot(new ResourceHandlerSlot(itemHandler, itemHandler::set, COPY_RESULT_SLOT, 16, 8 + 69) {
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
			public ResourceLocation getNoItemIcon() {
				return ResourceLocation.fromNamespaceAndPath("enchantment_custom_table", "container/slot/empty_slot_book");
			}
		});

		for (int si = 0; si < 3; ++si)
			for (int sj = 0; sj < 9; ++sj)
				this.addSlot(new Slot(inv, sj + (si + 1) * 9, 0 + 8 + sj * 18, 0 + 84 + si * 18 + 15));
		for (int si = 0; si < 9; ++si)
			this.addSlot(new Slot(inv, si, 0 + 8 + si * 18, 0 + 142 + 15));

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
			regenerateEnchantedBookSlot();
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

		if (index < 2 || index == TEMPLATE_BOOK_SLOT || index == COPY_RESULT_SLOT) {
			genEnchantedBookSlot();
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
				if (slot.mayPlace(p_38904_) && !itemstack.isEmpty() && ItemStack.isSameItemSameComponents(p_38904_, itemstack)) {
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
	}

	private final List<Holder<Enchantment>> allEnchantments = new ArrayList<>();

	public void tryGetAllEnchantments() {
		if (allEnchantments.isEmpty()) {
			Registry<Enchantment> fullEnchantmentList = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			fullEnchantmentList.asHolderIdMap().forEach(allEnchantments::add);
		}
	}

	public ItemStack getEnchantedBook(Holder<Enchantment> enchantment) {
		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);

		int enchantmentLevel = config().convertOnlyLevelOneBook() ? 1 : enchantment.value().getMaxLevel();
		enchantedBook.enchant(enchantment, enchantmentLevel);

		return enchantedBook;
	}

	public void setSearchQuery(String query, String clientLanguage, List<ResourceLocation> matchedEnchantments) {
		searchQuery = EnchantmentSearchRules.sanitizeSearchQuery(query);
		searchClientLanguage = EnchantmentSearchRules.sanitizeClientLanguage(clientLanguage);
		usingClientSearchMatches = !EnchantmentSearchRules.isBlankSearch(searchQuery) && matchedEnchantments != null;

		if (usingClientSearchMatches) {
			clientMatchedEnchantments = matchedEnchantments.stream()
					.limit(EnchantmentSearchRules.MAX_MATCHED_ENCHANTMENT_IDS)
					.collect(HashSet::new, Set::add, Set::addAll);
		} else {
			clientMatchedEnchantments = Set.of();
		}

		regenerateEnchantedBookSlot();
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public String getSearchClientLanguage() {
		return searchClientLanguage;
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
		if (page < 0 || page >= totalPage) {
			return;
		}
		currentPage = page;
		clearEnchantedBookSlot();
		genEnchantedBookSlot();
	}

	public void resetPage() {
		currentPage = 0;
		totalPage = 0;
	}

	public void clearEnchantedBookSlot() {
		for (int i = ENCHANTED_BOOK_SLOT_START; i < TEMPLATE_BOOK_SLOT; i++) {
			itemHandler.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	public void genEnchantedBookSlot() {
		if (isCopyMode()) {
			resetPage();
			clearEnchantedBookSlot();
			return;
		}

		List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
		boolean hasBook = itemHandler.getStackInSlot(0).is(Items.BOOK);
		boolean hasEnoughEmerald = hasEnoughPayment();

		if (!hasBook || !hasEnoughEmerald) {
			resetPage();
			clearEnchantedBookSlot();
			return;
		}

		for (int i = 0; i < ENCHANTED_BOOK_SLOT_SIZE; i++) {
			int slotIndex = i + ENCHANTED_BOOK_SLOT_START;
			int enchantmentIndex = i + currentPage * ENCHANTED_BOOK_SLOT_SIZE;

			if (enchantmentIndex < enchantments.size()) {
				if (itemHandler.getStackInSlot(slotIndex).isEmpty()) {
					Holder<Enchantment> enchantment = enchantments.get(enchantmentIndex);
					itemHandler.setStackInSlot(slotIndex, getEnchantedBook(enchantment));
				}
			} else {
				itemHandler.setStackInSlot(slotIndex, ItemStack.EMPTY);
			}
		}
	}

	public void regenerateEnchantedBookSlot() {
		if (isCopyMode()) {
			resetPage();
			clearEnchantedBookSlot();
			return;
		}
		currentPage = 0;
		rebuildEnchantedBookSlot();
	}

	private void rebuildEnchantedBookSlot() {
		List<Holder<Enchantment>> enchantments = getFilteredEnchantments();
		totalPage = EnchantmentTableRules.calculatePageCount(enchantments.size(), ENCHANTED_BOOK_SLOT_SIZE, false);
		if (currentPage >= totalPage) {
			currentPage = Math.max(totalPage - 1, 0);
		}
		clearEnchantedBookSlot();
		genEnchantedBookSlot();
	}

	private List<Holder<Enchantment>> getFilteredEnchantments() {
		tryGetAllEnchantments();
		if (EnchantmentSearchRules.isBlankSearch(searchQuery)) {
			return allEnchantments;
		}

		return allEnchantments.stream()
				.filter(this::matchesSearch)
				.toList();
	}

	private boolean matchesSearch(Holder<Enchantment> enchantment) {
		Optional<ResourceLocation> enchantmentId = EnchantmentUtils.getEnchantmentKey(world, enchantment).map(ResourceKey::location);
		if (enchantmentId.isPresent() && usingClientSearchMatches && clientMatchedEnchantments.contains(enchantmentId.get())) {
			return true;
		}

		List<String> serverCandidates = new ArrayList<>();
		enchantmentId.ifPresent(id -> {
			serverCandidates.add(id.toString());
			serverCandidates.add(id.getNamespace());
			serverCandidates.add(id.getPath());
			serverCandidates.add(id.getPath().replace('_', ' '));
			serverCandidates.add("enchantment." + id.getNamespace() + "." + id.getPath());
		});
		serverCandidates.add(enchantment.value().description().getString());
		return EnchantmentSearchRules.matchesAnyCandidate(searchQuery, serverCandidates);
	}

	public boolean canPickEnchantedBook() {
		return !isCopyMode() && itemHandler.getStackInSlot(0).is(Items.BOOK) && hasEnoughPayment();
	}

	private boolean hasEnoughPayment() {
		return EnchantmentTableRules.hasEnoughPayment(itemHandler.getStackInSlot(1), config());
	}

	public boolean pickEnchantedBook() {
		if (!canPickEnchantedBook()) {
			clearEnchantedBookSlot();
			return false;
		}

		itemHandler.getStackInSlot(0).shrink(1);
		EnchantmentTableRules.consumePayment(itemHandler.getStackInSlot(1), config());
		rebuildEnchantedBookSlot();
		return true;
	}

	private boolean isCopyMode() {
		return boundBlockEntity != null && boundBlockEntity.isCopyMode();
	}

	private TableConfigView config() {
		return Config.snapshot();
	}

	private static class ConversionMenuItemHandler extends MenuItemStackHandler {
		private final EnchantmentConversionTableBlockEntity blockEntity;
		private final ItemStackHandler fallbackPersistentHandler = new ItemStackHandler(EnchantmentConversionTableBlockEntity.SLOT_COUNT);

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
		void setStackInSlot(int slot, ItemStack stack) {
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
			int slotLimit = persistentHandler().getSlotLimit(toPersistentSlot(index));
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

		private IItemHandlerModifiable persistentHandler() {
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
}
