package com.phantomwing.theleadage.network;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** The raw lead settings a server sends its players ({@link com.phantomwing.theleadage.platform.LeadSettings}). */
public record LeadSettingsPayload(int waterRange, boolean campfireFumes, boolean rawLeadContact) implements CustomPacketPayload {
    public static final Type<LeadSettingsPayload> TYPE = new Type<>(TheLeadAge.resourceLocation("lead_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LeadSettingsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, LeadSettingsPayload::waterRange,
            ByteBufCodecs.BOOL, LeadSettingsPayload::campfireFumes,
            ByteBufCodecs.BOOL, LeadSettingsPayload::rawLeadContact,
            LeadSettingsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
