package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.effect.LeadFumes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The lead torch's slow poison: an open lead-salt flame. A player who lingers within {@value #RADIUS}
 * blocks catches a dose of Lead Sickness every ~{@value #LINGER_SCANS}s, climbing the same staged ladder
 * as lead-ore fumes — <b>Hunger → +Weakness → +Poison +Nausea</b>, one stage per dose (see
 * {@link LeadFumes#escalate}) — with a wisp of toxic fumes at each trigger. Leaving the radius resets the
 * build-up. Only players are affected; the enclosed lead lantern is the safe alternative.
 *
 * <p>Exposure build-up is transient (not saved): a torch forgets bystanders on chunk reload, which
 * just restarts the linger timer.</p>
 */
public class LeadTorchBlockEntity extends BlockEntity {
    private static final int SCAN_INTERVAL = 20;   // scan once a second
    private static final int LINGER_SCANS = 5;     // ~5s of staying nearby before each dose
    private static final double RADIUS = 2.5;

    /** Consecutive scans each nearby player has been in range (cleared the moment they leave). */
    private final Map<UUID, Integer> exposure = new HashMap<>();
    private int cooldown;

    public LeadTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEAD_TORCH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeadTorchBlockEntity torch) {
        if (++torch.cooldown < SCAN_INTERVAL || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        torch.cooldown = 0;

        Vec3 center = Vec3.atCenterOf(pos);
        Map<UUID, Integer> exposure = torch.exposure;
        Map<UUID, Integer> present = new HashMap<>();
        AABB box = AABB.ofSize(center, RADIUS * 2.0, RADIUS * 2.0, RADIUS * 2.0);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, box)) {
            if (player.isCreative() || player.isSpectator()
                    || player.position().distanceTo(center) > RADIUS) {
                continue;
            }
            int scans = exposure.getOrDefault(player.getUUID(), 0) + 1;
            if (scans >= LINGER_SCANS) {
                LeadFumes.escalate(player); // climb one Lead Sickness stage, then re-fume
                emitFumes(serverLevel, pos);
                scans = 0; // restart the linger timer for the next stage
            }
            present.put(player.getUUID(), scans);
        }
        // Only players still in range keep their build-up; stepping away resets it.
        exposure.clear();
        exposure.putAll(present);
    }

    /** A toxic wisp at the torch — a small version of lead ore's smoke + olive swirl burst. */
    private static void emitFumes(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.7, cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 5, 0.15, 0.1, 0.15, 0.01);
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.345f, 0.463f, 0.325f),
                cx, cy + 0.1, cz, 4, 0.25, 0.15, 0.25, 0.0);
    }
}
