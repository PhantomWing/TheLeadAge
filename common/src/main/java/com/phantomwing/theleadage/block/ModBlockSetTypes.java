package com.phantomwing.theleadage.block;

import com.phantomwing.theleadage.sound.ModSoundTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class ModBlockSetTypes {
    public static final BlockSetType LEAD = BlockSetType.register(new BlockSetType("lead",
            false,  // canOpenByHand — like iron: doors/trapdoors only open via redstone power
            false,  // canOpenByWindCharge
            false,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
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

    // Same as LEAD, but the leaded glass door/trapdoor open by hand (and wind charge) like a wooden one.
    public static final BlockSetType LEADED_GLASS = BlockSetType.register(new BlockSetType("leaded_glass",
            true,   // canOpenByHand
            true,   // canOpenByWindCharge
            false,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
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
}
