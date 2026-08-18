package vectorwing.farmersdelight.common.item;

import net.minecraft.world.item.Item;

/**
 * COMPILE-TIME STUB of Farmer's Delight Refabricated's {@code KnifeItem} — NOT the real class, and
 * excluded from the shipped jar (see {@code shadowJar { exclude 'vectorwing/**' }} in
 * {@code fabric/build.gradle}).
 *
 * <p><b>Why it exists.</b> The Fabric
 * {@code com.phantomwing.theleadage.fabric.compat.farmersdelight.LeadKnifeItem} extends FDR's
 * {@code KnifeItem}, but the FDR jar cannot be consumed as a dependency under architectury-loom
 * 1.7.435 (FDR is built with a newer fabric-loom — newer-Loom stamp plus a ClassTweaker access widener
 * that loom 1.7 cannot parse). This stub supplies only {@code KnifeItem}'s signature, in named
 * mappings, so the subclass compiles.</p>
 *
 * <p><b>Why it's safe.</b> The signature mirrors the real FDR class's constructor
 * {@code (Item.Properties)}. It is stripped from the published jar, so in a real
 * instance {@code LeadKnifeItem} binds to the genuine
 * {@code vectorwing.farmersdelight.common.item.KnifeItem}. In this mod's own dev runtime the stub is
 * present but never touched — FDR is absent there, so the {@code isModLoaded("farmersdelight")} guard
 * means {@code LeadKnifeItem} is never instantiated. The constructor body below never runs.</p>
 */
public class KnifeItem extends Item {
    // 1.21.5: FD's real KnifeItem takes only Properties (tools are plain Item + components).
    public KnifeItem(Item.Properties properties) {
        super(properties);
    }
}
