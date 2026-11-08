package com.phantomwing.theleadage.block;

import com.phantomwing.theleadage.sound.ModSoundTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class ModBlockSetTypes {
    public static final BlockSetType LEAD = BlockSetType.register(new BlockSetType("lead",
            false,  // canOpenByHand — like iron: doors/trapdoors only open via redstone power
            ModSoundTypes.LEAD,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON
        )
    );

    // Like LEAD, but the leaded glass door/trapdoor open by hand like a wooden one. 1.21 gives them the copper
    // door's and trapdoor's sounds, which 1.20.1 doesn't have (1.20.3's), so they open and close as iron's do.
    // (1.20.1 has no wind charge, and its block set types say nothing of arrows.)
    public static final BlockSetType LEADED_GLASS = BlockSetType.register(new BlockSetType("leaded_glass",
            true,   // canOpenByHand
            SoundType.COPPER,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON
        )
    );
}
