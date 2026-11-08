package com.phantomwing.theleadage.forge;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.TheLeadAgeCommon;
import com.phantomwing.theleadage.forge.client.TheLeadAgeForgeClient;
import com.phantomwing.theleadage.forge.condition.ModConditions;
import com.phantomwing.theleadage.forge.loot.ModLootModifiers;
import com.phantomwing.theleadage.forge.world.ModBiomeModifiers;
import com.phantomwing.theleadage.pack.DyedLeadedGlassPack;
import com.phantomwing.theleadage.platform.forge.CreativeTabPlatformImpl;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Forge entrypoint for The Lead Age. Loader-agnostic bootstrap via
 * {@link TheLeadAgeCommon#init()}, then the Forge config + (client-only)
 * client bootstrap.
 */
@Mod(TheLeadAgeCommon.MOD_ID)
public final class TheLeadAgeForge {
    public TheLeadAgeForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Architectury's registries and events on Forge need the mod's bus before anything registers.
        EventBuses.registerModEventBus(TheLeadAge.MOD_ID, modEventBus);

        // Config first: common code reads it as soon as it registers.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);

        TheLeadAgeCommon.init();

        // Config-gated lead-ore biome modifier serializer.
        ModBiomeModifiers.register(modEventBus);

        // Recipe-override condition ({@code theleadage:config_boolean}).
        ModConditions.register(modEventBus);

        // Global Loot Modifiers (lead horse armor replacing iron horse armor in loot).
        ModLootModifiers.register(modEventBus);

        // The files of the dyed leaded glass in a dye mod's colours, hidden from the pack screens.
        modEventBus.addListener((AddPackFindersEvent event) -> event.addRepositorySource(packs -> {
            Pack pack = DyedLeadedGlassPack.create(event.getPackType());
            if (pack != null) {
                packs.accept(hidden(pack, event.getPackType()));
            }
        }));

        // The vanilla creative tabs, filled from the mod's own bus so the mods mods.toml orders first (Vanilla
        // Backport) have listed what the lead items go after.
        modEventBus.addListener(CreativeTabPlatformImpl::buildContents);

        modEventBus.addListener(TheLeadAgeForge::commonSetup);

        // Client-only bootstrap (isolated so the server never loads the client classes).
        if (FMLEnvironment.dist.isClient()) {
            TheLeadAgeForgeClient.init(modEventBus);
        }
    }

    /**
     * {@code pack} kept off the pack screens. Forge 47 reads that from a pack's info (NeoForge has {@code Pack#hidden}),
     * so the pack is made again with its info marked hidden.
     */
    private static Pack hidden(Pack pack, PackType type) {
        Pack.Info info = Pack.readPackInfo(pack.getId(), id -> pack.open());
        if (info == null) {
            return pack;
        }
        Pack.Info hidden = new Pack.Info(info.description(), info.dataFormat(), info.resourceFormat(),
                info.requestedFeatures(), true);
        return Pack.create(pack.getId(), pack.getTitle(), pack.isRequired(), id -> pack.open(), hidden, type,
                pack.getDefaultPosition(), pack.isFixedPosition(), pack.getPackSource());
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // Vanilla's maps aren't thread-safe and this event runs in parallel with other mods', so on the main thread.
        event.enqueueWork(TheLeadAgeCommon::setup);
    }
}
