package vectorwing.farmersdelight.common.item;

import net.minecraft.world.item.Item;

/**
 * COMPILE-TIME STUB of Farmer's Delight Refabricated's {@code KnifeItem}. NOT the real class, and
 * excluded from the shipped jar (see {@code shadowJar { exclude 'vectorwing/**' }} in
 * {@code fabric/build.gradle}). At runtime {@code LeadKnifeItem} binds to the real FDR class.
 *
 * <p><b>Why a stub on 1.21.11.</b> Earlier lines took FDR as a {@code modCompileOnly} dependency so
 * javac checked this superclass shape against the real artifact. FDR's 1.21.11 builds ship a
 * {@code classTweaker v2} file, which the classtweaker reader bundled with architectury-loom
 * 1.14.476 cannot parse ("Unsupported class tweaker format: v2"), so the whole Fabric project fails
 * to configure with FDR on the classpath. The only loom that reads v2 is the 1.17.x line, which
 * belongs to the 26.1 build overhaul (Gradle 9 + Java 25) and is far past this branch.
 *
 * <p><b>What replaces that check.</b> The shape below was read straight out of
 * {@code farmers-delight-refabricated-1.21.11-3.6.16.jar} with javap: {@code KnifeItem extends
 * net.minecraft.class_1792} (Item) with a single {@code (class_1792$class_1793)} (Item.Properties)
 * constructor. Re-verify the same way when bumping {@code fdr_version}: a mismatch throws
 * {@code NoSuchMethodError} at item registration for players who have FDR installed, and never in a
 * dev run.</p>
 */
public class KnifeItem extends Item {
    public KnifeItem(Item.Properties properties) {
        super(properties);
    }
}
