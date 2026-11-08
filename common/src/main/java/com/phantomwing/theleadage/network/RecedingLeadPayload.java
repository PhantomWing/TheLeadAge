package com.phantomwing.theleadage.network;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Where raw lead used to leak, what it left in the water - the step its spreading had got to, and the longest route still
 * fouled - and a cut of 0 once it has gone ({@link com.phantomwing.theleadage.effect.RecedingLeads}).
 */
public record RecedingLeadPayload(BlockPos pos, int step, int cut) {
    public static final ResourceLocation ID = TheLeadAge.resourceLocation("receding_lead");

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(step);
        buf.writeVarInt(cut);
    }

    public static RecedingLeadPayload read(FriendlyByteBuf buf) {
        return new RecedingLeadPayload(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }
}
