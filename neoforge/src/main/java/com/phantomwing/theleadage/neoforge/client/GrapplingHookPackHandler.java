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
 * The Lead Grappling Hook's {@code items/} definitions, in a built-in pack listed only when Yori3o's Grappling Hooks is
 * loaded: they name that mod's own condition property, which fails to parse, with an error in the log, wherever it
 * isn't installed. Fabric registers the same pack from its own resources.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID, value = Dist.CLIENT)
public final class GrapplingHookPackHandler {
    private static final String PACK_ID = "builtin/" + TheLeadAge.MOD_ID + "/lead_grappling_hook";
    private static final String PACK_RESOURCE_ROOT = "resourcepacks/lead_grappling_hook";

    private GrapplingHookPackHandler() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(@NotNull AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded(ModIds.GRAPPLING_HOOKS)) {
            return;
        }

        Path packRoot = ModList.get().getModFileById(TheLeadAge.MOD_ID).getFile()
                .getContents().getContentRoots().stream()
                .map(root -> root.resolve(PACK_RESOURCE_ROOT))
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
        if (packRoot == null) {
            TheLeadAge.LOGGER.warn("Lead Grappling Hook pack root '{}' not found in the mod file; skipping.", PACK_RESOURCE_ROOT);
            return;
        }

        event.addRepositorySource(consumer -> {
            PackLocationInfo location = new PackLocationInfo(
                    PACK_ID,
                    Component.literal("The Lead Age: Lead Grappling Hook"),
                    PackSource.BUILT_IN,
                    Optional.empty());
            // Always on, as Fabric's ALWAYS_ENABLED pack is.
            PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);
            Pack pack = Pack.readMetaAndCreate(location,
                    new PathPackResources.PathResourcesSupplier(packRoot),
                    PackType.CLIENT_RESOURCES, selection);
            if (pack != null) {
                consumer.accept(pack);
            } else {
                TheLeadAge.LOGGER.warn("Lead Grappling Hook pack at '{}' has no readable metadata; skipping.", PACK_RESOURCE_ROOT);
            }
        });
    }
}
