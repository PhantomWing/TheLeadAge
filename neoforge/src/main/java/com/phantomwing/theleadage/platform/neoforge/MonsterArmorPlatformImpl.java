package com.phantomwing.theleadage.platform.neoforge;

import com.phantomwing.theleadage.armor.MonsterArmorHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * NeoForge implementation of {@link com.phantomwing.theleadage.platform.MonsterArmorPlatform}.
 *
 * <p>Subscribes the real {@code EntityJoinLevelEvent} and forwards its precise
 * {@code loadedFromDisk()} flag to the loader-agnostic equip logic.</p>
 */
public final class MonsterArmorPlatformImpl {
    private MonsterArmorPlatformImpl() {
    }

    public static void registerMobSpawnHandler() {
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event) ->
                MonsterArmorHandler.tryEquipLeadArmor(
                        event.getEntity(),
                        event.getLevel(),
                        event.loadedFromDisk()));
    }
}
