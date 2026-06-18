package com.phantomwing.theleadage.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.phantomwing.theleadage.TheLeadAge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TheLeadAge.MOD_ID);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> REPLACE_ITEM =
            LOOT_MODIFIERS.register("replace_item", ReplaceItemModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}
