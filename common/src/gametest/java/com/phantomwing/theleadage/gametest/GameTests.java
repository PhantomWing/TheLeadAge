package com.phantomwing.theleadage.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.List;
import java.util.function.Consumer;

/**
 * Every game test on this line, run on both modloaders. A test is its body and its entry here, and both
 * read the same on every line: how the list is registered changed at 1.21.5 and differs between the
 * modloaders, which belongs to each loader's {@code GameTestRegistration} alone, so porting a test never
 * touches a registration class or a JSON.
 */
public final class GameTests {
    public static final List<Test> ALL = List.of(
            test("lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::leadOreSometimesGivesLeadSickness),
            test("deepslate_lead_ore_sometimes_gives_lead_sickness", LeadOreGameTest::deepslateLeadOreSometimesGivesLeadSickness),
            test("raw_lead_block_sometimes_gives_lead_sickness", LeadOreGameTest::rawLeadBlockSometimesGivesLeadSickness),
            test("silk_touch_never_gives_lead_sickness", LeadOreGameTest::silkTouchNeverGivesLeadSickness),
            test("lead_sickness_ladder_escalates", LeadOreGameTest::leadSicknessLadderEscalates),
            test("lead_sickness_hisses_only_as_it_worsens", LeadOreGameTest::leadSicknessHissesOnlyAsItWorsens),
            test("campfire_fumes_only_over_raw_lead", LeadOreGameTest::campfireFumesOnlyOverRawLead),
            test("campfire_stream_doses_who_touches_it", LeadOreGameTest::campfireStreamDosesWhoTouchesIt),
            test("raw_lead_leaks_into_water", LeadOreGameTest::rawLeadLeaksIntoWater),
            test("raw_lead_doses_who_stands_on_it", LeadOreGameTest::rawLeadDosesWhoStandsOnIt).maxTicks(400),
            test("contaminated_water_doses_swimmer", LeadOreGameTest::contaminatedWaterDosesSwimmer).maxTicks(300),
            test("raw_lead_silk_touch_never_fumes", RawLeadGameTest::rawLeadSilkTouchNeverFumes),
            test("campfire_over_hay_over_raw_lead_fumes", RawLeadGameTest::campfireOverHayOverRawLeadFumes),
            test("campfire_stream_rises_with_the_smoke", RawLeadGameTest::campfireStreamRisesWithTheSmoke).maxTicks(300),
            test("raw_lead_spreading_steps_on", RawLeadGameTest::rawLeadSpreadingStepsOn).maxTicks(200),
            test("raw_lead_leaks_around_a_wall", RawLeadGameTest::rawLeadLeaksAroundAWall),
            test("raw_lead_edge_tints_without_poisoning", RawLeadGameTest::rawLeadEdgeTintsWithoutPoisoning),
            test("raw_lead_wakes_by_random_tick", RawLeadGameTest::rawLeadWakesByRandomTick),
            test("raw_lead_dries_out_without_an_update", RawLeadGameTest::rawLeadDriesOutWithoutAnUpdate),
            test("creepers_find_what_repels_them", RawLeadGameTest::creepersFindWhatRepelsThem),
            test("creepers_loaded_from_disk_avoid_lead_torches", RawLeadGameTest::creepersLoadedFromDiskAvoidLeadTorches),
            test("unrelated_effects_dont_advance_lead_sickness", RawLeadGameTest::unrelatedEffectsDontAdvanceLeadSickness),
            test("campfire_relit_keeps_its_rising_fumes", RawLeadGameTest::campfireRelitKeepsItsRisingFumes).maxTicks(300),
            test("lead_fumes_calm_no_bees", RawLeadGameTest::leadFumesCalmNoBees),
            test("lead_fumes_sicken_bees_at_their_hive", RawLeadGameTest::leadFumesSickenBeesAtTheirHive),
            test("raw_lead_doesnt_leak_into_water_under_it", LeadWaterGameTest::rawLeadDoesntLeakIntoWaterUnderIt),
            test("poison_never_climbs_a_waterfall", LeadWaterGameTest::poisonNeverClimbsAWaterfall),
            test("poison_runs_downstream_only", LeadWaterGameTest::poisonRunsDownstreamOnly),
            test("current_carries_poison_faster", LeadWaterGameTest::currentCarriesPoisonFaster),
            test("clean_inflow_lightens_poisoned_water", LeadWaterGameTest::cleanInflowLightensPoisonedWater),
            test("poisoned_water_darkens_from_its_edge", LeadWaterGameTest::poisonedWaterDarkensFromItsEdge),
            test("receding_lead_clears_from_its_edge", LeadWaterGameTest::recedingLeadClearsFromItsEdge),
            test("client_forgets_receding_it_hears_no_more_of", LeadWaterGameTest::clientForgetsRecedingItHearsNoMoreOf),
            test("contaminated_water_doses_mobs", LeadWaterGameTest::contaminatedWaterDosesMobs).maxTicks(200),
            test("coral_dies_in_poisoned_water", LeadWaterGameTest::coralDiesInPoisonedWater),
            test("swimming_in_poisoned_water_earns_its_advancement", LeadWaterGameTest::swimmingInPoisonedWaterEarnsItsAdvancement),
            test("poisoned_water_scores_worst_as_a_place_to_go", MobAvoidanceGameTest::poisonedWaterScoresWorstAsAPlaceToGo),
            test("fish_swim_around_poisoned_water", MobAvoidanceGameTest::fishSwimAroundPoisonedWater),
            test("axolotls_swim_around_poisoned_water", MobAvoidanceGameTest::axolotlsSwimAroundPoisonedWater),
            test("squid_turn_away_from_poisoned_water", MobAvoidanceGameTest::squidTurnAwayFromPoisonedWater),
            test("lead_gear_smelts_into_nuggets", LeadGearGameTest::leadGearSmeltsIntoNuggets),
            test("broken_lead_armor_releases_fumes", LeadGearGameTest::brokenLeadArmorReleasesFumes).maxTicks(150),
            test("lead_armor_weighs_on_mobs", LeadGearGameTest::leadArmorWeighsOnMobs),
            test("swapped_gear_keeps_its_wear", LeadGearGameTest::swappedGearKeepsItsWear),
            test("lead_trim_darkens_on_lead_armor", LeadGearGameTest::leadTrimDarkensOnLeadArmor),
            test("lead_armor_sets_its_slowness_once", LeadGearGameTest::leadArmorSetsItsSlownessOnce),
            test("armorers_buy_lead_in_rebalanced_worlds", LeadGearGameTest::armorersBuyLeadInRebalancedWorlds),
            test("lead_gear_turns_up_in_chests", LeadGearGameTest::leadGearTurnsUpInChests),
            test("trial_vaults_give_lead_ingots", LeadGearGameTest::trialVaultsGiveLeadIngots),
            test("lead_bulb_toggles_on_each_pulse", LeadBlocksGameTest::leadBulbTogglesOnEachPulse),
            test("beacon_beam_takes_the_pane_color", LeadBlocksGameTest::beaconBeamTakesThePaneColor),
            test("pistons_cant_push_solid_lead", LeadBlocksGameTest::pistonsCantPushSolidLead),
            test("lead_stairs_resist_blasts_as_lead", LeadBlocksGameTest::leadStairsResistBlastsAsLead),
            test("lead_wall_torch_drops_a_lead_torch", LeadBlocksGameTest::leadWallTorchDropsALeadTorch),
            test("wall_panes_block_paths_and_turn_with_the_build", LeadBlocksGameTest::wallPanesBlockPathsAndTurnWithTheBuild),
            test("pane_drops_keep_their_design", LeadBlocksGameTest::paneDropsKeepTheirDesign),
            test("door_recipe_combines", LeadOreGameTest::doorRecipeCombines),
            test("lead_ingot_recipes_dont_clash", LeadOreGameTest::leadIngotRecipesDontClash),
            test("lead_veins_carve_away", LeadOreGameTest::leadVeinsCarveAway),
            test("lead_weight_crushes_on_landing", LeadOreGameTest::leadWeightCrushesOnLanding),
            test("lead_weight_needs_build_permission", LeadOreGameTest::leadWeightNeedsBuildPermission),
            test("downward_dispenser_drops_the_weight", LeadOreGameTest::downwardDispenserDropsTheWeight),
            test("lead_weight_names_its_thrower", LeadOreGameTest::leadWeightNamesItsThrower),
            test("lead_data_outranks_neoforge", LeadOreGameTest::leadDataOutranksNeoForge),
            test("bars_connect_to_leaded_glass", LeadOreGameTest::barsConnectToLeadedGlass),
            test("frame_region_mapping", LeadOreGameTest::frameRegionMapping),
            test("glass_placement_stays_inside_panel", LeadOreGameTest::glassPlacementStaysInsidePanel),
            test("lead_weight_transforms_from_data", LeadOreGameTest::leadWeightTransformsFromData),
            test("lead_weight_tier_chain", LeadOreGameTest::leadWeightTierChain),
            test("lead_weight_break_chance", LeadOreGameTest::leadWeightBreakChance),
            test("lead_weight_keeps_its_start_when_saved", LeadOreGameTest::leadWeightKeepsItsStartWhenSaved),
            test("lead_weight_drops_into_hopper", LeadOreGameTest::leadWeightDropsIntoHopper).maxTicks(200),
            test("dispenser_places_lead_weight", LeadOreGameTest::dispenserPlacesLeadWeight).maxTicks(200),
            test("dynamic_panes_default_to_upright", LeadOreGameTest::dynamicPanesDefaultToUpright),
            test("lead_weight_hangs_from_vertical_chain", LeadOreGameTest::leadWeightHangsFromVerticalChain),
            test("lead_weight_detaches_from_horizontal_chain", LeadOreGameTest::leadWeightDetachesFromHorizontalChain),
            test("lead_weight_aim_direction", LeadOreGameTest::leadWeightAimDirection),
            test("lead_weight_vertical_offset", LeadOreGameTest::leadWeightVerticalOffset),
            test("armor_keeps_custom_attribute_modifiers", LeadOreGameTest::armorKeepsCustomAttributeModifiers),
            test("horse_armor_has_no_durability", LeadOreGameTest::horseArmorHasNoDurability),
            test("lead_tools_work_blocks", LeadOreGameTest::leadToolsWorkBlocks),
            test("lead_knife_fallback_keeps_sword_properties", LeadOreGameTest::leadKnifeFallbackKeepsSwordProperties),
            test("chunks_get_lead_veins_after_vanillas", LeadVeinGameTest::chunksGetLeadVeinsAfterVanillas),
            test("lead_veins_form_in_their_band", LeadVeinGameTest::leadVeinsFormInTheirBand),
            test("lead_settings_come_from_the_server", LeadSettingsGameTest::leadSettingsComeFromTheServer),
            test("lead_settings_survive_the_wire", LeadSettingsGameTest::leadSettingsSurviveTheWire),
            test("lead_pressure_plate_counts_mobs", LeadPressurePlateGameTest::leadPressurePlateCountsMobs).maxTicks(200),
            test("dyed_glass_pack_matches_datagen", DyedLeadedGlassGameTest::dyedGlassPackMatchesDatagen),
            test("dyed_glass_pack_opens", DyedLeadedGlassGameTest::dyedGlassPackOpens),
            test("glass_colours_read_as_names_or_numbers", DyedLeadedGlassGameTest::glassColoursReadAsNamesOrNumbers),
            test("dyed_glass_recipes_stay_data", DyedLeadedGlassGameTest::dyedGlassRecipesStayData),
            test("shattered_glass_is_its_colour", DyedLeadedGlassGameTest::shatteredGlassIsItsColour),
            test("dyed_glass_names_are_their_own", DyedLeadedGlassGameTest::dyedGlassNamesAreTheirOwn),
            test("clear_panes_take_a_dye", DyedLeadedGlassGameTest::clearPanesTakeADye),
            test("leaded_glass_door_glass_faces_the_placer", LeadedGlassPlacementGameTest::leadedGlassDoorGlassFacesThePlacer),
            test("leaded_glass_trapdoor_glass_swings_about_its_hinge",
                    LeadedGlassPlacementGameTest::leadedGlassTrapdoorGlassSwingsAboutItsHinge));

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
