package com.phantomwing.theleadage.network;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Where raw lead used to leak, what it left in the water - the step its spreading had got to, and the longest route still
 * fouled - and a cut of 0 once it has gone ({@link com.phantomwing.theleadage.effect.RecedingLeads}).
 */
public record RecedingLeadPayload(BlockPos pos, int step, int cut) implements CustomPacketPayload {
    public static final Type<RecedingLeadPayload> TYPE = new Type<>(TheLeadAge.resourceLocation("receding_lead"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RecedingLeadPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RecedingLeadPayload::pos,
            ByteBufCodecs.VAR_INT, RecedingLeadPayload::step,
            ByteBufCodecs.VAR_INT, RecedingLeadPayload::cut,
            RecedingLeadPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
