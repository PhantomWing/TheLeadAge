package com.phantomwing.theleadage.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.Item;

/**
 * {@code @ExpectPlatform} bridge that builds the Lead Knife.
 *
 * <p>Farmer's Delight is an <em>optional</em> dependency, but when it is present the knife should be a
 * real FD {@code KnifeItem} (Cutting Board support, the knife item abilities, the knife mining tag).
 * FD's {@code KnifeItem} is loader-specific and only on the classpath when FD is installed, so the
 * choice is made per loader:</p>
 * <ul>
 *   <li>FD loaded → the loader's {@code LeadKnifeItem}, which extends
 *       {@code vectorwing.farmersdelight.common.item.KnifeItem}.</li>
 *   <li>FD absent → a plain {@link Item} fallback (1.21.5: SwordItem is gone; the Properties
 *       already carry the sword components), so the item still
 *       exists (registry-consistent, multiplayer-safe), is still a usable weapon, and the mod loads
 *       standalone.</li>
 * </ul>
 *
 * <p>The item is always <em>registered</em>; only its concrete class is conditional. The
 * {@code Item.Properties} handed in already carry the knife attack attributes.</p>
 *
 * <p>Implemented per loader at {@code com.phantomwing.theleadage.platform.<loader>.KnifePlatformImpl}.</p>
 */
public final class KnifePlatform {
    private KnifePlatform() {
    }

    @ExpectPlatform
    public static Item createLeadKnife(Item.Properties properties) {
        throw new AssertionError("@ExpectPlatform stub – replaced per loader at build time");
    }
}
