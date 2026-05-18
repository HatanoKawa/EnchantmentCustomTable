package com.river_quinn.enchantment_custom_table.gametest;

import io.netty.buffer.Unpooled;
import com.river_quinn.enchantment_custom_table.Config;
import com.river_quinn.enchantment_custom_table.EnchantmentCustomTable;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantingCustomTableBlockEntity;
import com.river_quinn.enchantment_custom_table.block.entity.EnchantmentConversionTableBlockEntity;
import com.river_quinn.enchantment_custom_table.init.ModBlocks;
import com.river_quinn.enchantment_custom_table.utils.EnchantmentUtils;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantingCustomMenu;
import com.river_quinn.enchantment_custom_table.world.inventory.EnchantmentConversionMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber(modid = EnchantmentCustomTable.MODID)
public class BasicGameTests {
    private static final BlockPos ENCHANTING_CUSTOM_TABLE_POS = new BlockPos(1, 1, 1);
    private static final BlockPos ENCHANTMENT_CONVERSION_TABLE_POS = new BlockPos(3, 1, 1);
    private static final ResourceLocation EMPTY_TEMPLATE =
            ResourceLocation.fromNamespaceAndPath(EnchantmentCustomTable.MODID, "gametest/empty");
    private static final List<TestRegistration> TESTS = List.of(
            new TestRegistration("functional_blocks_create_their_block_entities", 20, BasicGameTests::functionalBlocksCreateTheirBlockEntities),
            new TestRegistration("functional_menus_bind_to_placed_blocks", 40, BasicGameTests::functionalMenusBindToPlacedBlocks),
            new TestRegistration("conversion_table_consumes_configured_payment_and_clears_results_when_exhausted", 40, BasicGameTests::conversionTableConsumesConfiguredPaymentAndClearsResultsWhenExhausted),
            new TestRegistration("conversion_table_search_filters_results_by_client_matched_ids", 40, BasicGameTests::conversionTableSearchFiltersResultsByClientMatchedIds),
            new TestRegistration("conversion_table_refills_taken_result_slot_after_pick", 40, BasicGameTests::conversionTableRefillsTakenResultSlotAfterPick),
            new TestRegistration("conversion_table_preserves_search_filter_after_pick", 40, BasicGameTests::conversionTablePreservesSearchFilterAfterPick),
            new TestRegistration("custom_table_splits_single_high_level_book_by_design", 40, BasicGameTests::customTableSplitsSingleHighLevelBookByDesign),
            new TestRegistration("custom_table_merges_duplicate_book_levels_without_vanilla_cap", 40, BasicGameTests::customTableMergesDuplicateBookLevelsWithoutVanillaCap),
            new TestRegistration("custom_table_quick_move_book_into_input_updates_tool", 40, BasicGameTests::customTableQuickMoveBookIntoInputUpdatesTool),
            new TestRegistration("custom_table_drag_insert_book_into_generated_slot_updates_tool", 40, BasicGameTests::customTableDragInsertBookIntoGeneratedSlotUpdatesTool),
            new TestRegistration("custom_table_removing_generated_book_subtracts_from_tool", 40, BasicGameTests::customTableRemovingGeneratedBookSubtractsFromTool),
            new TestRegistration("custom_table_quick_move_generated_book_subtracts_from_tool", 40, BasicGameTests::customTableQuickMoveGeneratedBookSubtractsFromTool)
    );

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        if (!isGameTestServerLaunch()) {
            return;
        }

        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                ResourceLocation.fromNamespaceAndPath(EnchantmentCustomTable.MODID, "default")
        );

        for (TestRegistration test : TESTS) {
            registerTest(event, environment, test);
        }
    }

    private static void registerTest(
            RegisterGameTestsEvent event,
            Holder<TestEnvironmentDefinition> environment,
            TestRegistration test
    ) {
        event.registerTest(
                testId(test.name()),
                new DirectGameTestInstance(
                        new TestData<>(environment, EMPTY_TEMPLATE, test.timeoutTicks(), 0, true),
                        test.function()
                )
        );
    }

    private static boolean isGameTestServerLaunch() {
        Boolean serverModLoaderResult = invokeBoolean(
                "net.neoforged.neoforge.server.loading.ServerModLoader",
                "isGameTestServer"
        );
        if (serverModLoaderResult != null) {
            return serverModLoaderResult;
        }

        return Boolean.TRUE.equals(invokeBoolean(
                "net.neoforged.neoforge.gametest.GameTestHooks",
                "isGametestServer"
        ));
    }

    private static Boolean invokeBoolean(String className, String methodName) {
        try {
            Object result = Class.forName(className, false, BasicGameTests.class.getClassLoader())
                    .getMethod(methodName)
                    .invoke(null);
            return result instanceof Boolean value ? value : null;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    private static ResourceLocation testId(String name) {
        return ResourceLocation.fromNamespaceAndPath(EnchantmentCustomTable.MODID, name);
    }

    private record TestRegistration(String name, int timeoutTicks, Consumer<GameTestHelper> function) {
    }

    private static final class DirectGameTestInstance extends GameTestInstance {
        private final Consumer<GameTestHelper> function;

        private DirectGameTestInstance(TestData<Holder<TestEnvironmentDefinition>> testData, Consumer<GameTestHelper> function) {
            super(testData);
            this.function = function;
        }

        @Override
        public void run(GameTestHelper helper) {
            function.accept(helper);
        }

        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return FunctionGameTestInstance.CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("direct");
        }
    }

    public static void functionalBlocksCreateTheirBlockEntities(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        helper.assertBlockPresent(ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get(), ENCHANTING_CUSTOM_TABLE_POS);
        assertTrue(helper,
                helper.getBlockEntity(ENCHANTING_CUSTOM_TABLE_POS, EnchantingCustomTableBlockEntity.class) != null,
                "Enchanting Custom Table should create its block entity"
        );

        helper.assertBlockPresent(ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get(), ENCHANTMENT_CONVERSION_TABLE_POS);
        assertTrue(helper,
                helper.getBlockEntity(ENCHANTMENT_CONVERSION_TABLE_POS, EnchantmentConversionTableBlockEntity.class) != null,
                "Enchantment Conversion Table should create its block entity"
        );

        helper.succeed();
    }
    public static void functionalMenusBindToPlacedBlocks(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos enchantingCustomTablePos = helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS);
        BlockPos enchantmentConversionTablePos = helper.absolutePos(ENCHANTMENT_CONVERSION_TABLE_POS);

        movePlayerTo(player, enchantingCustomTablePos);
        EnchantingCustomMenu enchantingCustomMenu = new EnchantingCustomMenu(
                1,
                player.getInventory(),
                menuData(enchantingCustomTablePos)
        );
        assertTrue(helper,
                enchantingCustomMenu.boundBlockEntity instanceof EnchantingCustomTableBlockEntity,
                "Enchanting Custom Table menu should bind to its block entity"
        );
        assertTrue(helper,
                enchantingCustomMenu.stillValid(player),
                "Enchanting Custom Table menu should be valid near its block"
        );

        movePlayerTo(player, enchantmentConversionTablePos);
        EnchantmentConversionMenu enchantmentConversionMenu = new EnchantmentConversionMenu(
                2,
                player.getInventory(),
                menuData(enchantmentConversionTablePos)
        );
        assertTrue(helper,
                enchantmentConversionMenu.boundBlockEntity instanceof EnchantmentConversionTableBlockEntity,
                "Enchantment Conversion Table menu should bind to its block entity"
        );
        assertTrue(helper,
                enchantmentConversionMenu.stillValid(player),
                "Enchantment Conversion Table menu should be valid near its block"
        );

        helper.succeed();
    }
    public static void conversionTableConsumesConfiguredPaymentAndClearsResultsWhenExhausted(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 3;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 3));

            assertTrue(helper, menu.totalPage > 0, "Conversion table should generate at least one page when funded");
            assertTrue(helper,
                    menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK),
                    "Conversion table should generate selectable enchanted books"
            );
            assertTrue(helper, menu.pickEnchantedBook(), "Picking a generated book should consume configured resources");
            assertTrue(helper, menu.getSlot(0).getItem().isEmpty(), "One normal book should be consumed");
            assertTrue(helper, menu.getSlot(1).getItem().isEmpty(), "Configured emerald payment should be consumed");
            assertTrue(helper, menu.totalPage == 0, "Generated results should clear when resources are exhausted");
            assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated result slots should be empty after resources run out");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }
    public static void conversionTableSearchFiltersResultsByClientMatchedIds(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 1;
        Config.minimumEmeraldBlockCost = 0;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ResourceLocation sharpnessId = enchantmentId(helper, sharpness);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 2));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD, 2));

            menu.setSearchQuery("localized-sharpness", "zh_cn", List.of(sharpnessId));

            assertTrue(helper, menu.totalPage == 1, "Client matched ids should narrow the conversion list to one page");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    sharpnessId,
                    sharpness.value().getMaxLevel(),
                    "Filtered conversion result should be the client-matched enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Only the matched enchantment should be shown");

            menu.setSearchQuery("definitely-missing-enchantment", "en_us", List.of());

            assertTrue(helper, menu.totalPage == 0, "No-match search should clear conversion pages");
            assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "No-match search should clear visible result slots");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }
    public static void conversionTableRefillsTakenResultSlotAfterPick(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 0;
        Config.minimumEmeraldBlockCost = 1;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 64));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD_BLOCK, 64));

            assertTrue(helper, menu.totalPage > 0, "Funded conversion table should generate result pages");
            assertTrue(helper, menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK), "First result slot should start filled");
            assertTrue(helper, menu.getSlot(3).getItem().is(Items.ENCHANTED_BOOK), "Second result slot should start filled");

            player.containerMenu = menu;
            menu.clicked(2, 0, ClickType.PICKUP, player);
            ItemStack taken = menu.getCarried();

            assertTrue(helper, taken.is(Items.ENCHANTED_BOOK), "Taking a result slot should return an enchanted book");
            assertTrue(helper, menu.getSlot(0).getItem().getCount() == 63, "Taking a result should consume one normal book");
            assertTrue(helper, menu.getSlot(1).getItem().getCount() == 63, "Taking a result should consume one emerald block");
            assertTrue(helper, menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK), "Taken result slot should be refilled immediately");
            assertTrue(helper, menu.getSlot(3).getItem().is(Items.ENCHANTED_BOOK), "Other visible result slots should remain filled");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }

    public static void conversionTablePreservesSearchFilterAfterPick(GameTestHelper helper) {
        helper.setBlock(ENCHANTMENT_CONVERSION_TABLE_POS, ModBlocks.ENCHANTMENT_CONVERSION_TABLE_BLOCK.get());

        int originalEmeraldCost = Config.minimumEmeraldCost;
        int originalEmeraldBlockCost = Config.minimumEmeraldBlockCost;
        boolean originalConvertMaxLevelBook = Config.convertMaxLevelBook;
        Config.minimumEmeraldCost = 0;
        Config.minimumEmeraldBlockCost = 1;
        Config.convertMaxLevelBook = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantmentConversionMenu menu = conversionMenu(helper, player);
            Holder<Enchantment> depthStrider = enchantment(helper, Enchantments.DEPTH_STRIDER);
            ResourceLocation depthStriderId = enchantmentId(helper, depthStrider);

            menu.getSlot(0).setByPlayer(new ItemStack(Items.BOOK, 64));
            menu.getSlot(1).setByPlayer(new ItemStack(Items.EMERALD_BLOCK, 64));
            menu.setSearchQuery("深海", "zh_cn", List.of(depthStriderId));

            assertTrue(helper, menu.totalPage == 1, "Single search match should keep one conversion page");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    depthStriderId,
                    depthStrider.value().getMaxLevel(),
                    "Filtered conversion result should start as the client-matched enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Search-filtered conversion results should not show unrelated enchantments");

            player.containerMenu = menu;
            menu.clicked(2, 0, ClickType.PICKUP, player);
            ItemStack taken = menu.getCarried();

            assertTrue(helper, taken.is(Items.ENCHANTED_BOOK), "Taking a filtered result should return an enchanted book");
            assertTrue(helper, menu.getSlot(0).getItem().getCount() == 63, "Taking a filtered result should consume one normal book");
            assertTrue(helper, menu.getSlot(1).getItem().getCount() == 63, "Taking a filtered result should consume one emerald block");
            assertTrue(helper, menu.totalPage == 1, "Taking a filtered result should preserve the search page count");
            assertEnchantmentIdLevel(
                    helper,
                    menu.getSlot(2).getItem(),
                    depthStriderId,
                    depthStrider.value().getMaxLevel(),
                    "Taking a filtered result should refill with the same filtered enchantment"
            );
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Taking a filtered result should not repopulate unrelated enchantments");

            helper.succeed();
        } finally {
            Config.minimumEmeraldCost = originalEmeraldCost;
            Config.minimumEmeraldBlockCost = originalEmeraldBlockCost;
            Config.convertMaxLevelBook = originalConvertMaxLevelBook;
        }
    }

    public static void customTableSplitsSingleHighLevelBookByDesign(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);

        menu.getSlot(0).setByPlayer(enchantedBook(sharpness, 8));

        assertTrue(helper, menu.totalPage == 1, "High-level single-enchantment books should generate one result page");
        assertEnchantmentLevel(helper, menu.getSlot(2).getItem(), sharpness, 4, "First split book should contain half the source level");
        assertEnchantmentLevel(helper, menu.getSlot(3).getItem(), sharpness, 2, "Second split book should contain the next binary level");
        assertEnchantmentLevel(helper, menu.getSlot(4).getItem(), sharpness, 1, "Third split book should contain the final unique level");
        assertTrue(helper, menu.getSlot(5).getItem().isEmpty(), "No duplicate split book should be generated");

        helper.succeed();
    }
    public static void customTableMergesDuplicateBookLevelsWithoutVanillaCap(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 4);

        menu.getSlot(0).setByPlayer(sword);
        menu.addEnchantment(enchantedBook(sharpness, 4), 1, true);

        assertEnchantmentLevel(
                helper,
                menu.getSlot(0).getItem(),
                sharpness,
                8,
                "Custom table should add duplicate enchantment levels instead of enforcing vanilla max level"
        );

        helper.succeed();
    }
    public static void customTableQuickMoveBookIntoInputUpdatesTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        boolean originalIgnoreLevelLimit = Config.ignoreEnchantmentLevelLimit;
        Config.ignoreEnchantmentLevelLimit = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);
            player.getInventory().setItem(0, enchantedBook(sharpness, 1));

            int firstHotbarSlot = EnchantingCustomMenu.ENCHANTMENT_CUSTOM_TABLE_SLOT_SIZE + 27;
            ItemStack moved = menu.quickMoveStack(player, firstHotbarSlot);

            assertTrue(helper, moved.is(Items.ENCHANTED_BOOK), "Quick-moving an enchanted book should move the book stack");
            assertTrue(helper, player.getInventory().getItem(0).isEmpty(), "Quick-moving should consume the source inventory book");
            assertTrue(helper, menu.getSlot(1).getItem().isEmpty(), "Quick-moving into the input book slot should leave that slot empty");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Quick-moving into the input book slot should merge into the tool");

            helper.succeed();
        } finally {
            Config.ignoreEnchantmentLevelLimit = originalIgnoreLevelLimit;
        }
    }
    public static void customTableDragInsertBookIntoGeneratedSlotUpdatesTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        boolean originalIgnoreLevelLimit = Config.ignoreEnchantmentLevelLimit;
        Config.ignoreEnchantmentLevelLimit = true;

        try {
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            EnchantingCustomMenu menu = enchantingMenu(helper, player);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 4);

            menu.getSlot(0).setByPlayer(sword);

            ItemStack carriedBook = enchantedBook(sharpness, 1);
            ItemStack remaining = menu.getSlot(3).safeInsert(carriedBook);
            menu.getSlot(3).setChanged();

            assertTrue(helper, remaining.isEmpty(), "Drag-inserting an enchanted book should consume the carried book");
            assertTrue(helper, menu.getSlot(3).getItem().isEmpty(), "Drag-inserting into a generated slot should leave that slot empty");
            assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 5, "Drag-inserting into a generated slot should merge into the tool");

            helper.succeed();
        } finally {
            Config.ignoreEnchantmentLevelLimit = originalIgnoreLevelLimit;
        }
    }
    public static void customTableRemovingGeneratedBookSubtractsFromTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 5);

        menu.getSlot(0).setByPlayer(sword);
        player.containerMenu = menu;

        menu.clicked(2, 0, ClickType.PICKUP, player);

        assertTrue(helper, player.containerMenu.getCarried().is(Items.ENCHANTED_BOOK), "Taking a generated book should put that book on the cursor");
        assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 0, "Removing a generated book should remove the matching tool enchantment");
        assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated books should clear after the source enchantment is removed");

        helper.succeed();
    }

    public static void customTableQuickMoveGeneratedBookSubtractsFromTool(GameTestHelper helper) {
        helper.setBlock(ENCHANTING_CUSTOM_TABLE_POS, ModBlocks.ENCHANTING_CUSTOM_TABLE_BLOCK.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        EnchantingCustomMenu menu = enchantingMenu(helper, player);
        Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 5);

        menu.getSlot(0).setByPlayer(sword);

        ItemStack moved = menu.quickMoveStack(player, 2);

        assertTrue(helper, moved.is(Items.ENCHANTED_BOOK), "Quick-moving a generated book should move that book stack");
        assertTrue(helper, player.getInventory().contains(moved), "Quick-moving a generated book should place it in the player inventory");
        assertEnchantmentLevel(helper, menu.getSlot(0).getItem(), sharpness, 0, "Quick-moving a generated book should remove the matching tool enchantment");
        assertTrue(helper, menu.getSlot(2).getItem().isEmpty(), "Generated books should clear after the source enchantment is quick-moved");

        helper.succeed();
    }

    private static FriendlyByteBuf menuData(BlockPos pos) {
        return new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos);
    }

    private static EnchantingCustomMenu enchantingMenu(GameTestHelper helper, Player player) {
        BlockPos pos = helper.absolutePos(ENCHANTING_CUSTOM_TABLE_POS);
        movePlayerTo(player, pos);
        return new EnchantingCustomMenu(1, player.getInventory(), menuData(pos));
    }

    private static EnchantmentConversionMenu conversionMenu(GameTestHelper helper, Player player) {
        BlockPos pos = helper.absolutePos(ENCHANTMENT_CONVERSION_TABLE_POS);
        movePlayerTo(player, pos);
        return new EnchantmentConversionMenu(2, player.getInventory(), menuData(pos));
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, net.minecraft.resources.ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElseThrow();
    }

    private static ResourceLocation enchantmentId(GameTestHelper helper, Holder<Enchantment> enchantment) {
        return EnchantmentUtils.getEnchantmentKey(helper.getLevel(), enchantment)
                .orElseThrow()
                .location();
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(enchantment, level);
        return book;
    }

    private static void assertEnchantmentLevel(
            GameTestHelper helper,
            ItemStack stack,
            Holder<Enchantment> enchantment,
            int expectedLevel,
            String message
    ) {
        int actualLevel = EnchantmentUtils.getEnchantments(stack).getLevel(enchantment);
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void assertEnchantmentIdLevel(
            GameTestHelper helper,
            ItemStack stack,
            ResourceLocation enchantmentId,
            int expectedLevel,
            String message
    ) {
        int actualLevel = 0;
        for (var entry : EnchantmentUtils.getEnchantments(stack).entrySet()) {
            ResourceLocation entryId = enchantmentId(helper, entry.getKey());
            if (enchantmentId.equals(entryId)) {
                actualLevel = entry.getIntValue();
                break;
            }
        }
        assertTrue(helper, actualLevel == expectedLevel, message + " (expected " + expectedLevel + ", got " + actualLevel + ")");
    }

    private static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, Component.literal(message));
    }

    private static void movePlayerTo(Player player, BlockPos pos) {
        player.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
