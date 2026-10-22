package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.component.LeadedGlassConfig;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Holds a leaded glass panel's per-region colours (dye ids; {@code -1} = clear). The frame
 * lives in the block state; only the colours — the unlimited axis — live here, and they're
 * applied at render time by the block colour provider (tinting), so no model explosion.
 */
public class LeadedGlassPanelBlockEntity extends BlockEntity {
    private List<Integer> colors = List.of(LeadedGlassConfig.CLEAR);

    public LeadedGlassPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEADED_GLASS_PANEL.get(), pos, state);
    }

    public List<Integer> getColors() {
        return colors;
    }

    /** Dye for {@code region}, or {@code null} if clear / out of range. */
    @Nullable
    public DyeColor colorAt(int region) {
        if (region < 0 || region >= colors.size()) {
            return null;
        }
        int id = colors.get(region);
        return id < 0 ? null : DyeColor.byId(id);
    }

    public void setColors(List<Integer> colors) {
        this.colors = colors.isEmpty() ? List.of(LeadedGlassConfig.CLEAR) : List.copyOf(colors);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Sets the colours with the block state whose clear regions go with them. The state picks each region's model and
     * the colours tint it, and both reach a client at the end of the tick, the state first: a newly dyed region drew
     * white for a frame before its colour arrived. So the colours are sent ahead of the state.
     */
    public void setColorsAndState(List<Integer> colors, BlockState state, int flags) {
        setColors(colors);
        if (level instanceof ServerLevel server && state != getBlockState()) {
            Packet<ClientGamePacketListener> packet = getUpdatePacket();
            for (ServerPlayer player : server.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(worldPosition), false)) {
                player.connection.send(packet);
            }
        }
        if (level != null) {
            level.setBlock(worldPosition, state, flags);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.colors = LeadedGlassConfig.getColors(input);
        // A live colour update (e.g. dyeing in place) arrives here on the client; the tints are baked
        // into the chunk mesh, so force a re-render. setBlocksDirty is gated by requiresRender(old,new),
        // which is false for the same state — so pass a sentinel old state to force the section re-mesh.
        if (level != null && level.isClientSide()) {
            level.setBlocksDirty(worldPosition, Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        LeadedGlassConfig.putColors(output, colors);
    }

    // ---- Client sync (colours must reach the client for tinting) ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 1.21.6: saveCustomOnly does the ValueOutput plumbing and runs saveAdditional for us.
        return saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
