package com.phantomwing.theleadage.neoforge.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Every game test on this line. A test is its body and its entry here, and both read the same on
 * every line: how the list is registered changed at 1.21.5 and belongs to {@link GameTestRegistration}
 * alone, so porting a test never touches a registration class or a JSON.
 */
public final class GameTests {
    public static final List<Test> ALL = List.of(
            test("lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::leadOreSometimesGivesLeadSickness),
            test("deepslate_lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::deepslateLeadOreSometimesGivesLeadSickness),
            test("silk_touch_never_gives_lead_sickness", LeadOreGameTest::silkTouchNeverGivesLeadSickness),
            test("lead_sickness_ladder_escalates", LeadOreGameTest::leadSicknessLadderEscalates),
            test("door_recipe_combines", LeadOreGameTest::doorRecipeCombines),
            test("bars_connect_to_leaded_glass", LeadOreGameTest::barsConnectToLeadedGlass),
            test("frame_region_mapping", LeadOreGameTest::frameRegionMapping),
            test("glass_placement_stays_inside_panel", LeadOreGameTest::glassPlacementStaysInsidePanel),
            test("lead_weight_transforms_from_data", LeadOreGameTest::leadWeightTransformsFromData),
            test("lead_weight_tier_chain", LeadOreGameTest::leadWeightTierChain),
            test("lead_weight_break_chance", LeadOreGameTest::leadWeightBreakChance),
            test("lead_weight_drops_into_hopper", LeadOreGameTest::leadWeightDropsIntoHopper).maxTicks(200),
            test("dispenser_places_lead_weight", LeadOreGameTest::dispenserPlacesLeadWeight).maxTicks(200),
            test("dynamic_panes_default_to_upright", LeadOreGameTest::dynamicPanesDefaultToUpright),
            test("lead_weight_hangs_from_vertical_chain", LeadOreGameTest::leadWeightHangsFromVerticalChain),
            test("lead_weight_detaches_from_horizontal_chain", LeadOreGameTest::leadWeightDetachesFromHorizontalChain),
            test("lead_weight_aim_direction", LeadOreGameTest::leadWeightAimDirection),
            test("lead_weight_vertical_offset", LeadOreGameTest::leadWeightVerticalOffset),
            test("armor_keeps_custom_attribute_modifiers", LeadOreGameTest::armorKeepsCustomAttributeModifiers));

    private GameTests() {
    }

    /** A required test in the mod's {@code empty} structure, with vanilla's default of 100 ticks. */
    private static Test test(String name, Consumer<GameTestHelper> body) {
        return new Test(name, body, "empty", 100);
    }

    /** One test: its id in the mod's namespace, its body, the structure it runs in and its time limit. */
    public record Test(String name, Consumer<GameTestHelper> body, String structure, int maxTicks) {
        public Test maxTicks(int ticks) {
            return new Test(name, body, structure, ticks);
        }
    }
}
