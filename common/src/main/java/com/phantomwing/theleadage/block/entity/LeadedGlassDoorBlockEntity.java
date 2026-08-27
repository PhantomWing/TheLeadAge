package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
import com.phantomwing.theleadage.component.LeadedGlassDoorConfig;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.config = new LeadedGlassDoorConfig(
                loadPane(input.childOrEmpty("Top")), loadPane(input.childOrEmpty("Bottom")));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        savePane(output.child("Top"), config.top());
        savePane(output.child("Bottom"), config.bottom());
    }

    private static LeadedGlassConfig loadPane(ValueInput tag) {
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

    /** 1.21.6: a nested compound is written by asking the parent for a child to fill in. */
    private static void savePane(ValueOutput out, LeadedGlassConfig pane) {
        out.putInt("Frame", pane.frame().ordinal());
        out.putIntArray("Colors", pane.colors().stream().mapToInt(Integer::intValue).toArray());
    }

    // ---- Client sync (the design must reach the client for the renderer) ----

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
