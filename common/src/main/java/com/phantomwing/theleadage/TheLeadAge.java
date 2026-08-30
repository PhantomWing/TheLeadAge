package com.phantomwing.theleadage;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Shared constants and helpers for The Lead Age. The actual mod bootstrap lives in
 * {@link TheLeadAgeCommon#init()} (called from each loader entrypoint).
 */
public final class TheLeadAge {
    public static final String MOD_ID = "theleadage";
    public static final Logger LOGGER = LogUtils.getLogger();

    private TheLeadAge() {
    }

    public static Identifier resourceLocation(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
