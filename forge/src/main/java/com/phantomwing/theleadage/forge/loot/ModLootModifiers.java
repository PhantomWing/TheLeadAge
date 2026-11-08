package com.phantomwing.theleadage.forge.loot;

import com.mojang.serialization.Codec;
import com.phantomwing.theleadage.TheLeadAge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TheLeadAge.MOD_ID);

    public static final RegistryObject<Codec<ReplaceItemModifier>> REPLACE_ITEM =
            LOOT_MODIFIERS.register("replace_item", ReplaceItemModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}
