package com.phantomwing.theleadage.forge.client;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.ModIds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Built-in resource pack carrying this mod's lead-themed texture for Create's
 * {@code create:crushed_raw_lead}.
 *
 * <p><b>Why a pack and not just {@code assets/create/...} in the mod?</b> Forge merges every
 * mod's assets into a <em>single</em> "Mod Resources" pack, so one mod cannot reliably out-prioritise
 * another's file inside it — dropping the texture at {@code assets/create/textures/item/} simply lost
 * to Create's own copy and never showed. A resource pack, by contrast, sits <em>above</em> Mod
 * Resources in the stack, so it wins. It is {@code required}, so it is force-selected at the very
 * first resource load (no reload, no options.txt entry), but not fixed — a user's own resource pack
 * can still be placed above it.</p>
 *
 * <p>Only registered when Create is actually loaded; otherwise the pack is pointless (the item it
 * retextures does not exist). Forge-only, because Create has no Fabric build here.</p>
 */
@Mod.EventBusSubscriber(modid = TheLeadAge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CreateOverridePackHandler {
    private static final String PACK_ID = "builtin/" + TheLeadAge.MOD_ID + "/create_overrides";
    private static final String PACK_RESOURCE_ROOT = "resourcepacks/create_overrides";

    private CreateOverridePackHandler() {
    }

    @SubscribeEvent
    public static void onAddPackFinders(@NotNull AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES || !ModList.get().isLoaded(ModIds.CREATE)) {
            return;
        }

        Path packRoot = ModList.get().getModFileById(TheLeadAge.MOD_ID).getFile().findResource(PACK_RESOURCE_ROOT);
        if (packRoot == null || !Files.exists(packRoot)) {
            TheLeadAge.LOGGER.warn("Create-override pack root '{}' not found in the mod file; skipping.", PACK_RESOURCE_ROOT);
            return;
        }

        event.addRepositorySource(consumer -> {
            // required=true  -> force-selected from the first load, so no reload is needed; at the top, but a user
            // pack may still be placed above it.
            Pack pack = Pack.readMetaAndCreate(PACK_ID, Component.literal("The Lead Age: Create Textures"), true,
                    id -> new PathPackResources(id, packRoot, true), PackType.CLIENT_RESOURCES, Pack.Position.TOP,
                    PackSource.BUILT_IN);
            if (pack != null) {
                consumer.accept(pack);
            } else {
                TheLeadAge.LOGGER.warn("Create-override pack at '{}' has no readable metadata; skipping.", PACK_RESOURCE_ROOT);
            }
        });
    }
}
