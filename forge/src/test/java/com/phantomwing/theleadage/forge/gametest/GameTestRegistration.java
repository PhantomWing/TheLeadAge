package com.phantomwing.theleadage.forge.gametest;

import com.phantomwing.theleadage.TheLeadAge;
import com.phantomwing.theleadage.gametest.GameTests;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;

/**
 * Registers {@link GameTests} the way versions before 1.21.5 do: as one generator, which Forge finds
 * through the holder annotation. From 1.21.5 this class registers into the test registries instead.
 * Nothing here changes when a test is added.
 */
@GameTestHolder(TheLeadAge.MOD_ID)
public final class GameTestRegistration {
    /** Public: 1.20.1's GameTestRegistry makes an instance of a generator's class before it calls the generator. */
    public GameTestRegistration() {
    }

    @GameTestGenerator
    public static List<TestFunction> generate() {
        return GameTests.ALL.stream()
                .map(test -> new TestFunction("defaultBatch", test.name(),
                        TheLeadAge.MOD_ID + ":" + test.structure(), test.maxTicks(), 0, true, test.body()))
                .toList();
    }
}
