package com.phantomwing.theleadage.fabric.client.compat;

import com.blackgear.platform.client.GameRendering;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.item.ModItems;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * The Lead Spear's held model where Vanilla Backport makes it a spear. Client-only, apart from the spear itself, so a
 * dedicated server never resolves a client type.
 */
public final class LeadSpearModels {
    /** The model the spear is drawn with in the hand. */
    public static final ModelResourceLocation IN_HAND = new ModelResourceLocation(TheLeadAge.resourceLocation("lead_spear_in_hand"), "inventory");

    private LeadSpearModels() {
    }

    /**
     * Vanilla Backport draws only its own spears held as spears; its Platform library draws the Lead Spear's
     * {@code _in_hand} model in the hand and its own one elsewhere. Called from the client entrypoint, before the first
     * resource load, where Platform adds the model to those to bake.
     */
    public static void registerHeldModel() {
        GameRendering.registerHandHeldModels(event -> event.register(ModItems.LEAD_SPEAR.get(), IN_HAND));
    }
}
