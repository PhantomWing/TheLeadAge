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

    // A Lead Bulb toggled, "Lead Bulb turns on", where vanilla's would name copper.
    public static final RegistrySupplier<SoundEvent> LEAD_BULB_TURN_ON = register("block.lead_bulb.turn_on");
    public static final RegistrySupplier<SoundEvent> LEAD_BULB_TURN_OFF = register("block.lead_bulb.turn_off");
    // Lead armor put on, "Lead armor clanks" for iron's.
    public static final RegistrySupplier<SoundEvent> ARMOR_EQUIP_LEAD = register("item.armor.equip_lead");
    // A Lead Weight chipping down a tier, shattering and landing, which vanilla's sounds name the anvil and the mace.
    public static final RegistrySupplier<SoundEvent> LEAD_WEIGHT_CHIP = register("block.lead_weight.chip");
    public static final RegistrySupplier<SoundEvent> LEAD_WEIGHT_BREAK = register("block.lead_weight.break");
    public static final RegistrySupplier<SoundEvent> LEAD_WEIGHT_LAND = register("block.lead_weight.land");
    // A leaded glass pane's design turned and a panel fitted, which the item frame's sounds name the item frame.
    public static final RegistrySupplier<SoundEvent> LEADED_GLASS_PANE_ROTATE = register("block.leaded_glass_pane.rotate");
    public static final RegistrySupplier<SoundEvent> LEADED_GLASS_PANE_FILL = register("block.leaded_glass_pane.fill");

    private ModSounds() {
    }

    private static RegistrySupplier<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TheLeadAge.resourceLocation(name)));
    }

    public static void register() {
        SOUNDS.register();
    }
}
