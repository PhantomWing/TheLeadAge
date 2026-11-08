package com.phantomwing.theleadage.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.UUID;

/**
 * The calls a test makes that each modloader answers its own way, each behind a signature both share. Forge's
 * and Fabric's test source sets each have this class, so a test body compiles on both unchanged.
 */
public final class TestLoader {
    private TestLoader() {
    }

    /**
     * The colour Forge's beacon takes from the block at {@code pane} for a beam from {@code beacon}, or null. Forge 47
     * gives it as three floats, which this packs as the test's {@code 0xFFRRGGBB}.
     */
    public static Integer beaconColor(GameTestHelper helper, BlockPos pane, BlockPos beacon) {
        float[] rgb = helper.getBlockState(pane).getBeaconColorMultiplier(helper.getLevel(), helper.absolutePos(pane),
                helper.absolutePos(beacon));
        return rgb == null ? null : 0xFF000000 | Math.round(rgb[0] * 255.0F) << 16 | Math.round(rgb[1] * 255.0F) << 8
                | Math.round(rgb[2] * 255.0F);
    }

    /**
     * A player in the test's level, who mines and is found by the fumes as a real one is: vanilla's
     * {@code makeMockServerPlayerInLevel} elsewhere. Forge 47's {@code PlayerList.placeNewPlayer} reaches for the mock
     * player's connection's channel, which it hasn't got, so the player is added to the level itself, with a profile of
     * its own each time so tests running side by side don't share one. Not a Forge {@code FakePlayer}: Forge's
     * {@code PlayerAdvancements.award} refuses one every advancement. Its connection has no channel and queues
     * whatever is sent to it.
     */
    public static ServerPlayer mockServerPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player"));
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player);
        level.addNewPlayer(player);
        return player;
    }
}
