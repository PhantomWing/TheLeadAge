package com.phantomwing.theleadage.platform.fabric;

/**
 * Fabric implementation of {@link com.phantomwing.theleadage.platform.MonsterArmorPlatform}
 * (resolved by Architectury's {@code @ExpectPlatform} transformer).
 *
 * <p>The monster-armor hook on Fabric is the mixin
 * {@code com.phantomwing.theleadage.fabric.mixin.PersistentEntitySectionManagerMixin}
 * (registered in {@code theleadage.mixins.json}), which hooks the vanilla
 * {@code PersistentEntitySectionManager#addEntity(EntityAccess, boolean)} and
 * forwards the authoritative "loaded from storage" boolean — giving Fabric the
 * exact same {@code loadedFromDisk} semantics NeoForge gets from
 * {@code EntityJoinLevelEvent.loadedFromDisk()}.</p>
 *
 * <p>That mixin self-registers via the Fabric mixin config, so this
 * {@code @ExpectPlatform} site has nothing to wire at runtime — it intentionally
 * no-ops. The method is kept so the {@code @ExpectPlatform} contract still
 * resolves on Fabric.</p>
 */
public final class MonsterArmorPlatformImpl {
    private MonsterArmorPlatformImpl() {
    }

    public static void registerMobSpawnHandler() {
        // No-op: the equip hook is the PersistentEntitySectionManagerMixin
        // (registered via theleadage.mixins.json), which carries the real
        // loaded-from-storage flag. Nothing to register here at runtime.
    }
}
