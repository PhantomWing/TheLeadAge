package com.phantomwing.theleadage.neoforge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Built-in resource pack that draws Mekanism's own lead - its ingot, nugget, raw lead, ores, blocks and ore processing
 * items - in The Lead Age's colours, for a pack where both mods' lead stand side by side.
 *
 * <p>Unlike {@link CreateOverridePackHandler}'s, it is off until a player or a modpack turns it on in the resource
 * packs screen: Mekanism's lead is its own mod's look, which this mod only offers to replace. Mekanism's slurries are
 * tinted in its code, so they keep Mekanism's colour. Only listed when Mekanism is loaded.</p>
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MekanismLeadPackHandler {
    private static final String PACK_ID = "builtin/" + TheLeadAge.MOD_ID + "/mekanism_lead";
    private static final String PACK_RESOURCE_ROOT = "resourcepacks/mekanism_lead";

    private MekanismLeadPackHandler() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(@NotNull AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded(ModIds.MEKANISM)) {
            return;
        }

        Path packRoot = ModList.get().getModFileById(TheLeadAge.MOD_ID).getFile().findResource(PACK_RESOURCE_ROOT);
        if (packRoot == null || !Files.exists(packRoot)) {
            TheLeadAge.LOGGER.warn("Mekanism lead pack root '{}' not found in the mod file; skipping.", PACK_RESOURCE_ROOT);
            return;
        }

        event.addRepositorySource(consumer -> {
            PackLocationInfo location = new PackLocationInfo(
                    PACK_ID,
                    Component.literal("The Lead Age: Mekanism Lead"),
                    PackSource.BUILT_IN,
                    Optional.empty());
            PackSelectionConfig selection = new PackSelectionConfig(false, Pack.Position.TOP, false);

            Pack pack = Pack.readMetaAndCreate(location,
                    new PathPackResources.PathResourcesSupplier(packRoot),
                    PackType.CLIENT_RESOURCES, selection);
            if (pack != null) {
                consumer.accept(pack);
            } else {
                TheLeadAge.LOGGER.warn("Mekanism lead pack at '{}' has no readable metadata; skipping.", PACK_RESOURCE_ROOT);
            }
        });
    }
}
