package com.phantomwing.theleadage.gametest;

import com.phantomwing.theleadage.network.LeadSettingsPayload;
import com.phantomwing.theleadage.platform.LeadSettings;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;

import static com.phantomwing.theleadage.gametest.TestCompat.fail;

/**
 * The raw lead settings a server sends its players ({@link LeadSettings}): what a client draws by once they arrive, and
 * what goes over the wire. That the server sends them as a player joins takes a client that can receive them, which a
 * mock player isn't; the scenarios played on a dedicated server see that. Listed in {@link GameTests}.
 */
public final class LeadSettingsGameTest {
    private LeadSettingsGameTest() {
    }

    /**
     * A client draws by the settings its server sent, and the server's own side keeps its config; once the client leaves,
     * it draws by its own config again. Only a change asks the client to draw its water again.
     */
    public static void leadSettingsComeFromTheServer(GameTestHelper helper) {
        LeadSettingsPayload config = LeadSettings.ofConfig();
        LeadSettingsPayload server = new LeadSettingsPayload(config.waterRange() == 3 ? 4 : 3, !config.campfireFumes(),
                !config.rawLeadContact());
        String problem;
        try {
            boolean changed = LeadSettings.received(server);
            boolean changedAgain = LeadSettings.received(server);
            problem = !changed ? "settings unlike the client's own didn't count as a change"
                    : changedAgain ? "the same settings sent twice counted as a change"
                    : LeadSettings.waterRange(true) != server.waterRange() ? "the client draws water " + LeadSettings.waterRange(true)
                    + " blocks out, not the server's " + server.waterRange()
                    : LeadSettings.campfireFumes(true) != server.campfireFumes() ? "the client doesn't take the server's campfire fumes"
                    : LeadSettings.rawLeadContact(true) != server.rawLeadContact() ? "the client doesn't take the server's raw lead contact"
                    : LeadSettings.waterRange(false) != config.waterRange() ? "the server side took the settings it was sent"
                    : null;
        } finally {
            LeadSettings.forget();
        }
        if (problem == null && LeadSettings.waterRange(true) != config.waterRange()) {
            problem = "a client that left the server still draws by its settings";
        }
        if (problem != null) {
            fail(helper, problem);
        } else {
            helper.succeed();
        }
    }

    /** The settings read back as they were written, every field in its place. */
    public static void leadSettingsSurviveTheWire(GameTestHelper helper) {
        RegistryFriendlyByteBuf wire = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        LeadSettingsPayload sent = new LeadSettingsPayload(7, false, true);
        LeadSettingsPayload.CODEC.encode(wire, sent);
        LeadSettingsPayload read = LeadSettingsPayload.CODEC.decode(wire);
        if (!read.equals(sent)) {
            fail(helper, "sent " + sent + ", read " + read);
        } else if (wire.isReadable()) {
            fail(helper, wire.readableBytes() + " bytes were left unread");
        } else {
            helper.succeed();
        }
    }
}
