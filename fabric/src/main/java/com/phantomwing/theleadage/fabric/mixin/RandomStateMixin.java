package com.phantomwing.theleadage.fabric.mixin;

import com.phantomwing.theleadage.world.vein.LeadVeinHolder;
import com.phantomwing.theleadage.world.vein.LeadVeins;
import net.minecraft.world.level.levelgen.RandomState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Carries the level's lead vein functions, bound to its seed (see {@link LeadVeins#bind}). */
@Mixin(RandomState.class)
public abstract class RandomStateMixin implements LeadVeinHolder {
    @Unique
    @Nullable
    private LeadVeins.Functions theleadage$veins;

    @Override
    @Nullable
    public LeadVeins.Functions theleadage$leadVeins() {
        return theleadage$veins;
    }

    @Override
    public void theleadage$setLeadVeins(LeadVeins.Functions functions) {
        theleadage$veins = functions;
    }
}
