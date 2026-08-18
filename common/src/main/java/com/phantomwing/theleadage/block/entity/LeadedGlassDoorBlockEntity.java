package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a leaded glass door's design: a pane per half ({@link LeadedGlassDoorConfig#top()} in the
 * upper half, {@code bottom} in the lower). The whole design lives on the block entity (present on
 * both halves) and is drawn by {@code LeadedGlassDoorRenderer}, so the door keeps the plain vanilla
 * door state set.
 */
public class LeadedGlassDoorBlockEntity extends BlockEntity {
    private LeadedGlassDoorConfig config = LeadedGlassDoorConfig.DEFAULT;

    public LeadedGlassDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEADED_GLASS_DOOR.get(), pos, state);
    }

    public LeadedGlassDoorConfig getConfig() {
        return config;
    }

    public void setConfig(LeadedGlassDoorConfig config) {
        this.config = config;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.config = new LeadedGlassDoorConfig(
                loadPane(tag.getCompoundOrEmpty("Top")), loadPane(tag.getCompoundOrEmpty("Bottom")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Top", savePane(config.top()));
        tag.put("Bottom", savePane(config.bottom()));
    }

    private static LeadedGlassConfig loadPane(CompoundTag tag) {
        LeadedGlassFrame frame = LeadedGlassFrame.values()[
                Math.floorMod(tag.getIntOr("Frame", 0), LeadedGlassFrame.values().length)];
        List<Integer> colors = new ArrayList<>();
        for (int id : tag.getIntArray("Colors").orElse(new int[0])) {
            colors.add(id);
        }
        if (colors.isEmpty()) {
            colors = List.of(LeadedGlassConfig.CLEAR);
        }
        return new LeadedGlassConfig(frame, colors);
    }

    private static CompoundTag savePane(LeadedGlassConfig pane) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Frame", pane.frame().ordinal());
        tag.putIntArray("Colors", pane.colors().stream().mapToInt(Integer::intValue).toArray());
        return tag;
    }

    // ---- Client sync (the design must reach the client for the renderer) ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
