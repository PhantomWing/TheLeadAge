package vectorwing.farmersdelight.common.item;

import net.minecraft.world.item.Item;

/**
 * COMPILE-TIME STUB of Farmer's Delight Refabricated's {@code KnifeItem}. NOT the real class, and
 * excluded from the shipped jar (see {@code shadowJar { exclude 'vectorwing/**' }} in
 * {@code fabric/build.gradle}). At runtime {@code LeadKnifeItem} binds to the real FDR class.
 *
 * <p>FDR is not resolved as a dependency on this branch, so nothing checks this shape
 * automatically. It was read out of {@code farmers-delight-refabricated-26.1-3.6.25.jar} with
 * javap instead: {@code KnifeItem extends Item}, one {@code (Item.Properties)} constructor.
 * Re-verify the same way when bumping {@code fdr_version}: a mismatch throws
 * {@code NoSuchMethodError} at item registration for players who have FDR installed, and never in
 * a dev run.</p>
 */
public class KnifeItem extends Item {
    public KnifeItem(Item.Properties properties) {
        super(properties);
    }
}
