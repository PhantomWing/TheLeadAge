package com.phantomwing.theleadage.neoforge.gametest;

import com.phantomwing.theleadage.TheLeadAge;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 1.21.5 replaced annotation-scanned gametests ({@code @GameTestHolder}/{@code @GameTest}) with
 * registries: each test body is a {@code Consumer<GameTestHelper>} in the static TEST_FUNCTION
 * registry (registered here), and each runnable test is a {@code test_instance} datapack entry
 * pairing a function with its structure/timeout data (the JSONs in
 * {@code data/theleadage/test_instance/}, plus the no-op {@code test_environment/default.json}).
 * The data route is deliberate: NeoForge's {@code RegisterGameTestsEvent} fires at server-starting
 * with the datapack registries already frozen (21.5.97), so code registration crashes. Vanilla
 * ships no test_instance data, so the game-test server runs exactly the mod's tests.
 */
@EventBusSubscriber(modid = TheLeadAge.MOD_ID)
public final class GameTestRegistration {
    /** Keep in sync with the JSONs in {@code data/theleadage/test_instance/}. */
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();

    static {
        TESTS.put("lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::leadOreSometimesGivesLeadSickness);
        TESTS.put("deepslate_lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::deepslateLeadOreSometimesGivesLeadSickness);
        TESTS.put("silk_touch_never_gives_lead_sickness", LeadOreGameTest::silkTouchNeverGivesLeadSickness);
        TESTS.put("lead_sickness_ladder_escalates", LeadOreGameTest::leadSicknessLadderEscalates);
        TESTS.put("door_recipe_combines", LeadOreGameTest::doorRecipeCombines);
        TESTS.put("lead_bricks_recipe_yields_one", LeadOreGameTest::leadBricksRecipeYieldsOne);
        TESTS.put("bars_connect_to_leaded_glass", LeadOreGameTest::barsConnectToLeadedGlass);
        TESTS.put("frame_region_mapping", LeadOreGameTest::frameRegionMapping);
        TESTS.put("glass_placement_stays_inside_panel", LeadOreGameTest::glassPlacementStaysInsidePanel);
        TESTS.put("door_glass_mirror_matches_frame", LeadOreGameTest::doorGlassMirrorMatchesFrame);
        TESTS.put("lead_weight_transforms_from_data", LeadOreGameTest::leadWeightTransformsFromData);
        TESTS.put("lead_weight_tier_chain", LeadOreGameTest::leadWeightTierChain);
        TESTS.put("lead_weight_break_chance", LeadOreGameTest::leadWeightBreakChance);
        TESTS.put("lead_weight_drops_into_hopper", LeadOreGameTest::leadWeightDropsIntoHopper);
        TESTS.put("dispenser_places_lead_weight", LeadOreGameTest::dispenserPlacesLeadWeight);
        TESTS.put("dynamic_panes_default_to_upright", LeadOreGameTest::dynamicPanesDefaultToUpright);
        TESTS.put("lead_weight_hangs_from_vertical_chain", LeadOreGameTest::leadWeightHangsFromVerticalChain);
        TESTS.put("lead_weight_detaches_from_horizontal_chain", LeadOreGameTest::leadWeightDetachesFromHorizontalChain);
        TESTS.put("lead_weight_aim_direction", LeadOreGameTest::leadWeightAimDirection);
        TESTS.put("lead_weight_vertical_offset", LeadOreGameTest::leadWeightVerticalOffset);
        TESTS.put("armor_keeps_custom_attribute_modifiers", LeadOreGameTest::armorKeepsCustomAttributeModifiers);
        TESTS.put("lead_knife_fallback_keeps_sword_properties", LeadOreGameTest::leadKnifeFallbackKeepsSwordProperties);
        TESTS.put("every_test_function_has_an_instance", LeadOreGameTest::everyTestFunctionHasAnInstance);
    }

    private GameTestRegistration() {
    }

    /** The registered test-function names, so a test can check each one has its test_instance JSON. */
    public static Set<String> testNames() {
        return Collections.unmodifiableSet(TESTS.keySet());
    }

    @SubscribeEvent
    public static void registerFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> TESTS.forEach(
                (name, function) -> helper.register(Identifier.fromNamespaceAndPath(TheLeadAge.MOD_ID, name), function)));
    }
}
