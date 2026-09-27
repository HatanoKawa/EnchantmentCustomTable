package com.river_quinn.enchantment_custom_table;

import com.river_quinn.enchantment_custom_table.config.TomlPaymentConfig;
import com.river_quinn.enchantment_custom_table.core.config.*;
import com.river_quinn.enchantment_custom_table.network.TableConfigSync;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.LoggerFactory;
import java.util.List;

//? if >=1.21.6 {
@EventBusSubscriber(modid = EnchantmentCustomTable.MODID)
//? } else {
/*@EventBusSubscriber(modid = EnchantmentCustomTable.MODID, bus = EventBusSubscriber.Bus.MOD)*/
//?}
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.IntValue VERSION = BUILDER.defineInRange("configVersion", 2, 2, 2);
    private static final ModConfigSpec.ConfigValue<List<?>> PAYMENTS = BUILDER
            .comment("Alternative payments: item_id and integer cost. Books/invalid entries are ignored; empty lists use defaults.",
                    "Costs clamp to 1..min(default item stack limit, 64). Delete an entry to disable it; zero means one.")
            .define("paymentOptions", () -> TomlPaymentConfig.entries(PaymentOptions.DEFAULTS), value -> value instanceof List<?>);
    private static final ModConfigSpec.BooleanValue ENFORCE = BUILDER
            .comment("Limit duplicate enchantment merges to vanilla maximum levels.").define("enforceEnchantmentLevelLimit", false);
    private static final ModConfigSpec.BooleanValue INCREMENTAL = BUILDER
            .comment("Merge equal enchantment levels by adding one level.").define("incrementalSameLevelMerge", false);
    private static final ModConfigSpec.BooleanValue LEVEL_ONE = BUILDER
            .comment("Only exchange level-one books.").define("convertOnlyLevelOneBook", false);
    private static final ModConfigSpec.BooleanValue FREE = BUILDER
            .comment("Require neither books nor payment items; both input slots reject insertion.").define("freeConversionTableCosts", false);
    public static final ModConfigSpec SPEC = BUILDER.build();
    public static final TableConfigState STATE = new TableConfigState();
    private static volatile TableConfigSnapshot raw = JsonTableConfigCodec.defaultSnapshot();

    public static void prepareMigration() {
        TomlPaymentConfig.migrate(FMLPaths.CONFIGDIR.get().resolve(EnchantmentCustomTable.MODID + "-common.toml"), Config::warn);
    }
    public static TableConfigSnapshot snapshot() { return STATE.local(); }
    public static TableConfigSnapshot snapshot(boolean clientSide) { return STATE.forSide(clientSide); }
    public static TableConfigSnapshot editableSnapshot() { return MinecraftPaymentConfig.resolve(raw, Config::warn); }
    public static TableConfigSnapshot defaultSnapshot() { return JsonTableConfigCodec.defaultSnapshot(); }
    public static void resolveLocal() { STATE.setLocal(MinecraftPaymentConfig.resolve(raw, Config::warn)); }
    public static void setRuntimeSnapshot(TableConfigSnapshot value) { STATE.setLocal(value); }
    public static void warn(String message) { LoggerFactory.getLogger(EnchantmentCustomTable.MODID).warn(message); }

    public static void save(TableConfigSnapshot value) {
        PAYMENTS.set(TomlPaymentConfig.entries(value.paymentOptions()));
        ENFORCE.set(value.enforceEnchantmentLevelLimit()); INCREMENTAL.set(value.incrementalSameLevelMerge());
        LEVEL_ONE.set(value.convertOnlyLevelOneBook()); FREE.set(value.freeConversionTableCosts());
        SPEC.save();
        readValues();
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC || event instanceof ModConfigEvent.Unloading) return;
        readValues();
    }
    private static void readValues() {
        raw = new TableConfigSnapshot(TomlPaymentConfig.read(PAYMENTS.get(), Config::warn),
                ENFORCE.get(), INCREMENTAL.get(), LEVEL_ONE.get(), FREE.get());
        TableConfigSync.configChanged();
    }
}
