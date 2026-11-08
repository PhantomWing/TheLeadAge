package com.phantomwing.theleadage.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.TrimMaterial;
import org.jetbrains.annotations.Nullable;

/**
 * The darker palette a lead trim takes on lead armour, as it does from 1.21. 1.20.1's {@code TrimMaterial} names its
 * darker palettes by vanilla's {@code ArmorMaterials} alone, and {@code ArmorTrim.getColorPaletteSuffix} reads them
 * only for those, so lead armour can't be named there: each loader's {@code ArmorTrimMixin} asks here first.
 */
public final class TrimPalettes {
    /** The palette in the {@code armor_trims} and {@code blocks} atlases' permutations. */
    public static final String LEAD_DARKER = "lead_darker";

    private TrimPalettes() {
    }

    /** The palette {@code trim} draws with on armour of {@code armor}, or null for the trim's own. */
    @Nullable
    public static String overrideFor(Holder<TrimMaterial> trim, ArmorMaterial armor) {
        return armor == ModArmorMaterials.LEAD && trim.is(ModTrimMaterials.LEAD) ? LEAD_DARKER : null;
    }
}
