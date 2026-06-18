package com.phantomwing.theleadage.sound;

import net.minecraft.world.level.block.SoundType;

/**
 * Lead block sound types — plain vanilla {@link SoundType}s copied from existing
 * vanilla sound types with tweaked volume/pitch. Lead uses the iron block sound
 * ({@link SoundType#METAL}) but pitched well down so it reads deeper / heavier than
 * iron or silver. Every referenced sound is a built-in vanilla sound, so no custom
 * audio assets or registration are needed.
 */
public class ModSoundTypes {
    public static final SoundType LEAD = ofCopy(SoundType.METAL, 1.0f, 0.65f);
    // No iron grate exists, so the grate keeps the copper-grate sound but pitched
    // extra low (half-pitch) for a notably deeper, heavier grate than silver's.
    public static final SoundType LEAD_GRATE = ofCopy(SoundType.COPPER_GRATE, 1.0f, 0.5f);

    private static SoundType ofCopy(SoundType soundType, float volume, float pitch) {
        return new SoundType(
                soundType.getVolume() * volume,
                soundType.getPitch() * pitch,
                soundType.getBreakSound(),
                soundType.getStepSound(),
                soundType.getPlaceSound(),
                soundType.getHitSound(),
                soundType.getFallSound());
    }
}
