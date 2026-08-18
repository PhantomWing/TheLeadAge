package com.phantomwing.theleadage.platform.neoforge;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.neoforge.compat.farmersdelight.LeadKnifeItem;
import dev.architectury.platform.Platform;
import net.minecraft.world.item.Item;

/**
 * NeoForge implementation of {@link com.phantomwing.theleadage.platform.KnifePlatform}
 * (resolved by Architectury's {@code @ExpectPlatform} transformer).
 */
public final class KnifePlatformImpl {
    private KnifePlatformImpl() {
    }

    public static Item createLeadKnife(Item.Properties properties) {
        // Only touch LeadKnifeItem (-> FD's KnifeItem) when FD is actually loaded, so the mod still
        // loads standalone with the plain-Item fallback. Call the static factory (invokestatic) rather
        // than `new LeadKnifeItem` here: an inline `new` makes the verifier load LeadKnifeItem's
        // FD-only superclass while verifying THIS method, crashing without FD.
        if (Platform.isModLoaded(ModIds.FARMERS_DELIGHT)) {
            return LeadKnifeItem.create(properties);
        }
        return new Item(properties); // sword components already on the Properties (1.21.5)
    }
}
