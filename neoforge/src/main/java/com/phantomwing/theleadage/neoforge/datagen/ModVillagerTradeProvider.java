package com.phantomwing.theleadage.neoforge.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import com.phantomwing.theleadage.neoforge.Configuration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Writes the lead villager_trade JSON for each smith profession (gated by enable_villager_trades)
 * plus an additive level-2 pool tag entry per profession.
 *
 * <p>26.1 turned villager trades from code-defined {@code VillagerTrades.ItemListing} factories
 * into a datapack {@code villager_trade} registry with {@code tags/villager_trade/<profession>/level_N}
 * pools, and NeoForge dropped its {@code VillagerTradesEvent}. There is no vanilla datagen provider
 * for the new registry, and the datapack-registry path cannot attach load conditions to an entry, so
 * this small provider encodes the trade through its own CODEC (schema-correct by construction) and
 * then injects the {@code neoforge:conditions} gate. {@link FabricConditionsProvider} mirrors that
 * to {@code fabric:load_conditions}, so one file gates identically on both loaders.</p>
 */
public class ModVillagerTradeProvider implements DataProvider {
    /** Mirror vanilla: all three smiths buy lead ingots at apprentice, exactly like iron ingots. */
    private static final List<String> SMITHS = List.of("armorer", "toolsmith", "weaponsmith");
    private static final int LEVEL = 2;
    private static final String TRADE_NAME = "lead_ingot_emerald";
    /** Standard villager price multiplier (how much demand shifts the price). */
    private static final float PRICE_MULTIPLIER = 0.05f;

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public ModVillagerTradeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        this.packOutput = packOutput;
        this.registries = registries;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return registries.thenCompose(provider -> {
            Path data = packOutput.getOutputFolder(PackOutput.Target.DATA_PACK);
            RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
            List<CompletableFuture<?>> futures = new ArrayList<>();

            // Buy 4 Lead Ingot, sell 1 Emerald (maxUses 12, villagerXp 10).
            VillagerTrade trade = new VillagerTrade(
                    new TradeCost(ModItems.LEAD_INGOT.get(), 4),   // wants
                    new ItemStackTemplate(Items.EMERALD),          // gives
                    12, 10, PRICE_MULTIPLIER,
                    Optional.empty(), List.of());
            JsonObject tradeJson = VillagerTrade.CODEC.encodeStart(ops, trade).getOrThrow().getAsJsonObject();
            tradeJson.add("neoforge:conditions", configCondition(Configuration.ENABLE_VILLAGER_TRADES_ID));

            for (String smith : SMITHS) {
                futures.add(DataProvider.saveStable(cache, tradeJson.deepCopy(),
                        data.resolve(tradePath(smith))));
                futures.add(DataProvider.saveStable(cache, poolTag(smith),
                        data.resolve("minecraft/tags/villager_trade/" + smith + "/level_" + LEVEL + ".json")));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        });
    }

    /** {@code data/theleadage/villager_trade/<profession>/2/lead_ingot_emerald.json} */
    private static String tradePath(String profession) {
        return TheLeadAge.MOD_ID + "/villager_trade/" + profession + "/" + LEVEL + "/" + TRADE_NAME + ".json";
    }

    /** {@code theleadage:config_boolean} condition array gating on the given setting. */
    private static JsonArray configCondition(String settingId) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", TheLeadAge.MOD_ID + ":config_boolean");
        condition.addProperty("settingId", settingId);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        return conditions;
    }

    /**
     * Additive level-2 pool tag adding our trade as an optional entry. Optional so the pool
     * tolerates the trade being condition-removed when the config gates it out.
     */
    private static JsonObject poolTag(String profession) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", TheLeadAge.MOD_ID + ":" + profession + "/" + LEVEL + "/" + TRADE_NAME);
        entry.addProperty("required", false);
        JsonArray values = new JsonArray();
        values.add(entry);
        JsonObject tag = new JsonObject();
        tag.add("values", values);
        return tag;
    }

    @Override
    public @NotNull String getName() {
        return "The Lead Age Villager Trades";
    }
}
