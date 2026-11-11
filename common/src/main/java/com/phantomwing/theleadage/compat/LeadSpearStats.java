package com.phantomwing.theleadage.compat;

/**
 * The Lead Spear's numbers, in the order 1.21.11's {@code Item.Properties#spear} takes them, which both spear backports
 * take too: the netherite spear's charge, swung 0.2 slower like the rest of the set. The first number is the swing's
 * length in seconds and the attack speed is its inverse: netherite's 1.15 is 0.87, so 1.5 gives 0.67. The 1.21.11
 * line's own spear has the same.
 */
public final class LeadSpearStats {
    public static final float ATTACK_DURATION = 1.5f;
    public static final float DAMAGE_MULTIPLIER = 1.2f;
    public static final float DELAY = 0.4f;
    public static final float DISMOUNT_TIME = 2.5f;
    public static final float DISMOUNT_THRESHOLD = 7.0f;
    public static final float KNOCKBACK_TIME = 5.5f;
    public static final float KNOCKBACK_THRESHOLD = 5.1f;
    public static final float DAMAGE_TIME = 8.75f;
    public static final float DAMAGE_THRESHOLD = 4.6f;

    private LeadSpearStats() {
    }
}
