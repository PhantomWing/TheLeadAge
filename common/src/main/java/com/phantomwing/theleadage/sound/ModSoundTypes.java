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
    public static final SoundType LEAD = ofCopy(SoundType.METAL, 1.1f, 0.3f);
    // 1.20.1 has no copper grate or bulb sounds (1.21's), so the grate and the bulb copy metal's, as The Silver Age's
    // do on this version, pitched half as high as metal.
    public static final SoundType LEAD_GRATE = ofCopy(SoundType.METAL, 1.1f, 0.5f);
    // Lead Weight: dense netherite-block sounds pitched down so placing/breaking it reads
    // as a weighty hunk of lead. (The falling/landing sounds are played in code.)
    public static final SoundType LEAD_WEIGHT = ofCopy(SoundType.NETHERITE_BLOCK, 1.0f, 0.5f);
    public static final SoundType LEAD_BULB = ofCopy(SoundType.METAL, 1.1f, 0.5f);

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
