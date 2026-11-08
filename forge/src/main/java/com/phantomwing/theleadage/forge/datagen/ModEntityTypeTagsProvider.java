package com.phantomwing.theleadage.forge.datagen;

import net.minecraft.resources.ResourceLocation;
import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.tags.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TheLeadAge.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        // Mobs that can naturally spawn wearing iron armor.
        this.tag(ModTags.EntityTypes.CAN_WEAR_LEAD_ARMOR)
            .add(EntityType.ZOMBIE)
            .add(EntityType.ZOMBIE_VILLAGER)
            .add(EntityType.HUSK)
            .add(EntityType.SKELETON)
            .add(EntityType.STRAY)
            // No bogged: it is 1.21's.
            // 1.21.11's desert skeleton, as the 1.21.11 lines have it, where Vanilla Backport adds it under its id.
            .addOptional(new ResourceLocation("parched"));
        // Mobs that can naturally spawn holding an iron sword, axe or shovel.
        this.tag(ModTags.EntityTypes.CAN_HOLD_LEAD_TOOLS)
            .add(EntityType.ZOMBIE)
            .add(EntityType.ZOMBIE_VILLAGER)
            .add(EntityType.HUSK)
            .add(EntityType.VINDICATOR);
    }
}
