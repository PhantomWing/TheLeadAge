package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.damage.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** Takes the lookup with this mod's own damage types in it, {@link ModDatapackProvider#getRegistryProvider()}. */
public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // A lead weight landing on someone is a falling anvil's blow: the helmet takes it and softens
        // it, and a shield can't stop it.
        tag(DamageTypeTags.DAMAGES_HELMET).add(ModDamageTypes.LEAD_WEIGHT);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.LEAD_WEIGHT);
    }
}
