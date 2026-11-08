package com.phantomwing.theleadage.network;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** The raw lead settings a server sends its players ({@link com.phantomwing.theleadage.platform.LeadSettings}). */
public record LeadSettingsPayload(int waterRange, boolean campfireFumes, boolean rawLeadContact) {
    public static final ResourceLocation ID = TheLeadAge.resourceLocation("lead_settings");

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(waterRange);
        buf.writeBoolean(campfireFumes);
        buf.writeBoolean(rawLeadContact);
    }

    public static LeadSettingsPayload read(FriendlyByteBuf buf) {
        return new LeadSettingsPayload(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }
}
