package com.phantomwing.theleadage.block.entity;

import com.phantomwing.theleadage.block.custom.LeadedGlassFrame;
import com.phantomwing.theleadage.component.LeadedGlassConfig;
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

import java.util.List;

/** Holds a leaded glass trapdoor's glass design (frame + colours); drawn by LeadedGlassTrapdoorRenderer. */
public class LeadedGlassTrapdoorBlockEntity extends BlockEntity {
    private LeadedGlassConfig config = new LeadedGlassConfig(LeadedGlassFrame.PLAIN, List.of(LeadedGlassConfig.CLEAR));

    public LeadedGlassTrapdoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEADED_GLASS_TRAPDOOR.get(), pos, state);
    }

    public LeadedGlassConfig getConfig() {
        return config;
    }

    public void setConfig(LeadedGlassConfig config) {
        this.config = config;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        LeadedGlassFrame frame = LeadedGlassFrame.values()[Math.floorMod(input.getIntOr("Frame", 0), LeadedGlassFrame.values().length)];
        this.config = new LeadedGlassConfig(frame, LeadedGlassConfig.getColors(input));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Frame", config.frame().ordinal());
        LeadedGlassConfig.putColors(output, config.colors());
    }

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
