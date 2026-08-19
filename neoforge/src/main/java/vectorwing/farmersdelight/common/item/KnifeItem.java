package vectorwing.farmersdelight.common.item;

import net.minecraft.world.item.Item;

/**
 * COMPILE-TIME STUB of Farmer's Delight's {@code KnifeItem}. NOT the real class, and excluded from
 * the shipped jar (see {@code shadowJar { exclude 'vectorwing/**' }} in {@code neoforge/build.gradle}).
 *
 * <p><b>Why it exists.</b> {@code com.phantomwing.theleadage.neoforge.compat.farmersdelight.LeadKnifeItem}
 * extends FD's {@code KnifeItem}, and this supplies that one signature, in named mappings, so the
 * subclass compiles without taking FD on as a dependency.</p>
 *
 * <p><b>Status on 1.21.5.</b> Farmer's Delight publishes no NeoForge build for this version, so the
 * {@code isModLoaded("farmersdelight")} guard can never be true here and the class below is dead
 * weight kept only for parity with the Fabric side. It is stripped from the published jar either
 * way. If an FD-NeoForge 1.21.5 build appears, audit this constructor against its real
 * {@code KnifeItem} before shipping: a mismatch throws {@code NoSuchMethodError} at item
 * registration for players who have FD installed, and never in a dev run.</p>
 */
public class KnifeItem extends Item {
    // 1.21.5: FD's real KnifeItem takes only Properties (tools are plain Item + components).
    public KnifeItem(Item.Properties properties) {
        super(properties);
    }
}
