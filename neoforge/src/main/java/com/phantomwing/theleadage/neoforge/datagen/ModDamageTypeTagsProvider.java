package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.damage.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

/** Takes the lookup with this mod's own damage types in it, not the vanilla one the other tag providers get. */
public class ModDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheLeadAge.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // A lead weight landing on someone is a falling anvil's blow: the helmet takes it and softens
        // it, and a shield can't stop it.
        tag(DamageTypeTags.DAMAGES_HELMET).add(ModDamageTypes.LEAD_WEIGHT);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.LEAD_WEIGHT);
    }
}
