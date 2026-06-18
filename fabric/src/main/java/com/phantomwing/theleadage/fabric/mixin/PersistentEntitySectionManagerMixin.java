package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.armor.MonsterArmorHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fabric parity for the NeoForge monster-armor disk-load gating.
 *
 * <p>NeoForge subscribes {@code EntityJoinLevelEvent} and forwards the real
 * {@code event.loadedFromDisk()} flag to
 * {@link MonsterArmorHandler#tryEquipLeadArmor(Entity, net.minecraft.world.level.Level, boolean)}.
 * This mixin gives Fabric the same flag by hooking the single vanilla path that
 * carries the authoritative "loaded from storage" boolean:</p>
 *
 * <pre>{@code
 * net.minecraft.world.level.entity.PersistentEntitySectionManager
 *     #addEntity(T extends EntityAccess, boolean)   // 1.21.1 Mojmap
 *     // descriptor: (Lnet/minecraft/world/level/entity/EntityAccess;Z)Z
 * }</pre>
 *
 * <p>The boolean is vanilla's "loaded from storage" flag: {@code addNewEntity(T)}
 * and the world-gen consumer both call {@code addEntity(e, false)} (fresh /
 * world-gen spawns — matching NeoForge, whose {@code loadedFromDisk()} is
 * {@code false} for world-gen), while chunk-deserialization calls
 * {@code addEntity(e, true)}. {@code PersistentEntitySectionManager} is the
 * <em>server</em> entity manager, so this never runs client-side; the
 * {@code level.isClientSide()} guard inside {@link MonsterArmorHandler} is kept
 * regardless.</p>
 */
@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerMixin {
    @Inject(
            method = "addEntity(Lnet/minecraft/world/level/entity/EntityAccess;Z)Z",
            at = @At("HEAD")
    )
    private void theleadage$equipLeadArmor(EntityAccess entityAccess,
                                           boolean loadedFromStorage,
                                           CallbackInfoReturnable<Boolean> cir) {
        // PersistentEntitySectionManager<T> is parameterised with
        // net.minecraft.world.entity.Entity on the server level, and Entity
        // implements EntityAccess, so this cast is always safe at runtime.
        if (entityAccess instanceof Entity entity) {
            MonsterArmorHandler.tryEquipLeadArmor(entity, entity.level(), loadedFromStorage);
        }
    }
}
