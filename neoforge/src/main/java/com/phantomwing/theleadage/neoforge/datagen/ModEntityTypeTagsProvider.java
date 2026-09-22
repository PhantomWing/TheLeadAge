package com.phantomwing.theleadage.neoforge.datagen;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import net.minecraft.tags.TagKey;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    /**
     * 26.2: TagAppender#add takes a ResourceKey rather than the value, so this hands the chain
     * below a wrapper that still accepts entity types. See ValueAppender.
     */
    @Override
    protected ValueAppender<EntityType<?>> tag(TagKey<EntityType<?>> tag) {
        return new ValueAppender<>(super.tag(tag), value -> value.builtInRegistryHolder().key());
    }

    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TheLeadAge.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Mobs that can naturally spawn wearing iron armor.
        this.tag(ModTags.EntityTypes.CAN_WEAR_LEAD_ARMOR)
            .add(EntityTypes.ZOMBIE)
            .add(EntityTypes.ZOMBIE_VILLAGER)
            .add(EntityTypes.SKELETON)
            .add(EntityTypes.STRAY)
            .add(EntityTypes.BOGGED);
    }
}
