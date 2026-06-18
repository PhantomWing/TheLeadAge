package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * {@code @ExpectPlatform} bridge for the entity-join hook backing
 * {@link com.phantomwing.theleadage.armor.MonsterArmorHandler}.
 *
 * <p>NeoForge has no Architectury-equivalent for {@code EntityJoinLevelEvent}'s
 * {@code loadedFromDisk()} flag (Architectury's {@code EntityEvent.ADD} only
 * exposes the entity + level). To preserve exact behaviour on both loaders, the
 * subscription itself is loader-specific.</p>
 */
public final class MonsterArmorPlatform {
    private MonsterArmorPlatform() {
    }

    /**
     * Subscribes the loader's entity-join event and dispatches to
     * {@code MonsterArmorHandler.tryEquipLeadArmor(...)}.
     *
     * <p>NeoForge: subscribes {@code EntityJoinLevelEvent} and forwards its real
     * {@code loadedFromDisk()} value.<br>
     * Fabric: no-op here — the hook is the {@code PersistentEntitySectionManager}
     * mixin, which carries the authoritative loaded-from-storage flag.</p>
     */
    @ExpectPlatform
    public static void registerMobSpawnHandler() {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }
}
