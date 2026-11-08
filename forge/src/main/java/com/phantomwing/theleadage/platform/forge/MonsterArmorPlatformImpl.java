package com.phantomwing.theleadage.platform.forge;

import com.phantomwing.theleadage.armor.MonsterArmorHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/**
 * Forge implementation of {@link com.phantomwing.theleadage.platform.MonsterArmorPlatform}.
 *
 * <p>Subscribes the real {@code EntityJoinLevelEvent} and forwards its precise
 * {@code loadedFromDisk()} flag to the loader-agnostic equip logic.</p>
 */
public final class MonsterArmorPlatformImpl {
    private MonsterArmorPlatformImpl() {
    }

    public static void registerMobSpawnHandler() {
        MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) ->
                MonsterArmorHandler.tryEquipLeadGear(
                        event.getEntity(),
                        event.getLevel(),
                        event.loadedFromDisk()));
    }
}
