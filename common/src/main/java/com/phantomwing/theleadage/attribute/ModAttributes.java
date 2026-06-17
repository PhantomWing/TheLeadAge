package com.phantomwing.theleadage.attribute;

import com.phantomwing.theleadage.TheLeadAge;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Mod attributes. {@code Heaviness} is how much an item weighs the wearer down —
 * lead armor grants it (shown as "+N Heaviness" in the tooltip) and
 * {@code LeadArmorItem} derives its movement-speed, gravity and water-sink
 * penalties from how much Heaviness the wearer is carrying.
 */
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(TheLeadAge.MOD_ID, Registries.ATTRIBUTE);

    public static final RegistrySupplier<Attribute> HEAVINESS = ATTRIBUTES.register("heaviness",
            () -> new RangedAttribute("attribute.name.theleadage.heaviness", 0.0, 0.0, 1024.0).setSyncable(true));

    public static void register() {
        ATTRIBUTES.register();
    }
}
