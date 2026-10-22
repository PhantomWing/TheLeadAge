package com.phantomwing.theleadage.sound;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

/**
 * The mod's own sound events. Each plays vanilla sounds ({@code assets/theleadage/sounds.json}), so it ships no audio, and
 * is its own event so its subtitle can say what it is: vanilla's fizz under its own name would read "Fire extinguished".
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(TheLeadAge.MOD_ID, Registries.SOUND_EVENT);

    /** The hiss as lead takes hold of a player: its fumes, raw lead stood on, or the water it poisons. */
    public static final RegistrySupplier<SoundEvent> LEAD_FUMES_HISS = SOUNDS.register("lead_fumes.hiss",
            () -> SoundEvent.createVariableRangeEvent(TheLeadAge.resourceLocation("lead_fumes.hiss")));

    private ModSounds() {
    }

    public static void register() {
        SOUNDS.register();
    }
}
