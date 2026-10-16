package com.phantomwing.theleadage.platform;

import com.phantomwing.theleadage.network.LeadSettingsPayload;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * The settings for raw lead that the server decides and the client draws: how far raw lead contaminates water, whether
 * campfires over it give off fumes, whether it poisons who stands on it. The server reads its own config; a client
 * draws by what the server it plays on sent ({@code LeadSettingsSync}), so the water it tints is the water that
 * poisons, and only falls back on its own config while the server has said nothing.
 */
public final class LeadSettings {
    /** The furthest the config lets raw lead contaminate. */
    public static final int MAX_WATER_RANGE = 8;

    @Nullable
    private static volatile LeadSettingsPayload fromServer;

    private LeadSettings() {
    }

    /** How far raw lead contaminates the water around it, in blocks through the water; 0 turns it off. */
    public static int waterRange(boolean clientSide) {
        LeadSettingsPayload synced = clientSide ? fromServer : null;
        return synced != null ? synced.waterRange() : ofConfig().waterRange();
    }

    /** Whether a campfire over raw lead gives off fumes. */
    public static boolean campfireFumes(boolean clientSide) {
        LeadSettingsPayload synced = clientSide ? fromServer : null;
        return synced != null ? synced.campfireFumes() : CommonConfig.campfireLeadFumes();
    }

    /** Whether standing on raw lead poisons, and dust lifts off it. */
    public static boolean rawLeadContact(boolean clientSide) {
        LeadSettingsPayload synced = clientSide ? fromServer : null;
        return synced != null ? synced.rawLeadContact() : CommonConfig.rawLeadContact();
    }

    /** These settings as this side's config has them: what a server sends. */
    public static LeadSettingsPayload ofConfig() {
        return new LeadSettingsPayload(Mth.clamp(CommonConfig.leadWaterRange(), 0, MAX_WATER_RANGE),
                CommonConfig.campfireLeadFumes(), CommonConfig.rawLeadContact());
    }

    /** The server the client plays on sent its settings. Whether they differ from what the client drew by. */
    public static boolean received(LeadSettingsPayload settings) {
        LeadSettingsPayload before = fromServer;
        fromServer = settings;
        return before == null ? !settings.equals(ofConfig()) : !settings.equals(before);
    }

    /** The client left the server: its own config again until the next one says otherwise. */
    public static void forget() {
        fromServer = null;
    }
}
