package com.phantomwing.theleadage.platform.fabric;

import com.phantomwing.theleadage.compat.ModIds;
import com.phantomwing.theleadage.fabric.compat.farmersdelight.LeadKnifeItem;
import dev.architectury.platform.Platform;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * Fabric implementation of {@link com.phantomwing.theleadage.platform.KnifePlatform}
 * (resolved by Architectury's {@code @ExpectPlatform} transformer).
 */
public final class KnifePlatformImpl {
    /** FD's own knife damage (+0.5), and 0.2 slower to swing than its knives: 5.5 damage, 1.8 speed. */
    private static final float KNIFE_DAMAGE = 0.5f;
    private static final float KNIFE_SPEED = -2.2f;

    private KnifePlatformImpl() {
    }

    public static Item createLeadKnife(Item.Properties properties, Tier tier) {
        // Only touch LeadKnifeItem (-> FDR's KnifeItem) when FDR is actually loaded, so the mod still
        // loads standalone with the SwordItem fallback. Call the static factory (invokestatic) rather
        // than `new LeadKnifeItem` here: an inline `new` makes the verifier load LeadKnifeItem's
        // FDR-only superclass while verifying THIS method, crashing without FDR.
        if (Platform.isModLoaded(ModIds.FARMERS_DELIGHT)) {
            return LeadKnifeItem.create(tier, KNIFE_DAMAGE, KNIFE_SPEED, properties);
        }
        // SwordItem takes a whole number, so the fallback rounds the knife's 0.5 to 0, as The Silver Age's does.
        return new SwordItem(tier, 0, KNIFE_SPEED, properties);
    }
}
