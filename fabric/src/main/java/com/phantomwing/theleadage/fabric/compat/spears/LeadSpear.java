package com.phantomwing.theleadage.fabric.compat.spears;

import com.notunanancyowen.spears.Spears;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.compat.LeadSpearStats;
import com.phantomwing.theleadage.tool.ModTiers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/** The Lead Spear as Backported Spears makes a spear. Only loaded when it is installed. */
public final class LeadSpear {
    private LeadSpear() {
    }

    /**
     * Registers {@code spear} once Backported Spears has made its own spears. A spear is built of components and
     * sounds it registers in its own initializer, and Fabric Loader runs the mods' initializers in no set order,
     * whatever {@code suggests} says: built before them, the Lead Spear was a plain item. So it is registered with the
     * other items where Backported Spears started first, and otherwise with the next item after its Wooden Spear.
     */
    public static RegistrySupplier<Item> register(DeferredRegister<Item> items, String name, Supplier<Item> spear) {
        if (hasStarted()) {
            return items.register(name, spear);
        }
        ResourceLocation id = TheLeadAge.resourceLocation(name);
        RegistryEntryAddedCallback.event(BuiltInRegistries.ITEM).register(new RegistryEntryAddedCallback<>() {
            private boolean registered;

            @Override
            public void onEntryAdded(int rawId, ResourceLocation added, Item item) {
                if (!registered && hasStarted()) {
                    registered = true; // first, since registering the spear calls this again
                    Registry.register(BuiltInRegistries.ITEM, id, spear.get());
                }
            }
        });
        return RegistrarManager.get(TheLeadAge.MOD_ID).get(Registries.ITEM).delegate(id);
    }

    /** Backported Spears sets its Wooden Spear once what a spear is built of is registered, and before its other spears. */
    private static boolean hasStarted() {
        return Spears.WOODEN_SPEAR != null;
    }

    /** Only called once Backported Spears has started ({@link #register}). */
    public static Item create() {
        return Spears.registerSpearRaw(ModTiers.LEAD, LeadSpearStats.ATTACK_DURATION, LeadSpearStats.DAMAGE_MULTIPLIER,
                LeadSpearStats.DELAY, LeadSpearStats.DISMOUNT_TIME, LeadSpearStats.DISMOUNT_THRESHOLD,
                LeadSpearStats.KNOCKBACK_TIME, LeadSpearStats.KNOCKBACK_THRESHOLD, LeadSpearStats.DAMAGE_TIME,
                LeadSpearStats.DAMAGE_THRESHOLD);
    }
}
