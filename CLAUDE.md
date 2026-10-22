@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/26.1`

**This file describes the `version/26.1` line**: Minecraft 26.1.2 (the jar covers 26.1, 26.1.1 and 26.1.2), Fabric and NeoForge.
Built with Architectury Loom without remapping (`loom-no-remap`): Minecraft 26.x ships unobfuscated, so there are no mappings and `shadowJar` is the shipped jar.

It belongs to whichever folder has this branch checked out - the main `TheLeadAge` folder or a worktree
under `TheLeadAge/.worktrees/`. A session started in a worktree also loads the main folder's CLAUDE.md,
which describes another line; for this folder, this file is the one that applies. Confirm with
`git branch --show-current`. `MinecraftDeveloperPortal/data/mods.json` lists every line of the mod.

A vanilla-friendly expansion that adds lead, a new metal resource. Mod ID `theleadage`. A
sister mod to The Silver Age.

## Tests

The game tests are split so that nothing version-specific reaches a test body: the test classes and
`GameTests` (one entry per test) are the same on every line, `GameTestRegistration` changes only
with registration, and `TestCompat` holds each call whose shape differs between versions.
Porting a test is its body plus its `GameTests` entry, copied as-is; see `verification.md`.

`scenarios/` holds the play scenarios the portal's Play scenarios check runs as a player. They are
plain JSON and copy across lines unchanged. `scenarios/server` holds those its Play scenarios on a server check
plays on the line's dedicated server, which `scenarios/server/<loader>/config` gives a `lead_water_range` of 2
against the client's 6, so the tint shows whose setting the client draws by.

The test bodies are 1.21.1's but for seven calls the 1.2 port moved into `TestCompat`: six on 1.21.3 (`trim`,
`trimPalette`, `hasRecipe`, `recipesFor`, `use`, `nearestDirection`), which `version/1.21`'s bodies still make
directly, and `assertTrue` on 1.21.5, since `GameTestHelper.assertTrue` takes only a Component from 1.21.5 to
1.21.10, which the bodies of `version/1.21`, `version/1.21.3` and `version/1.21.4` still call on the helper. Their
`TestCompat`s want those methods in their own forms before the bodies match again. 1.21.11 has an eighth, `offer`
(`ItemListing.getOffer` takes the trader's level first from 1.21.11), which 26.1 can't have: trades are data from 26.1
and `ItemListing` is gone. So on 26.1 `armorers_buy_lead_in_rebalanced_worlds` asks
`TestCompat.rebalancedApprenticeArmorerBuys` instead, which reads the armorer's level 2 trade set and the mod's tag it
draws from, and `lead_gear_smelts_into_nuggets` cooks through `TestCompat.assemble`, which takes any recipe here
(`Recipe#assemble` lost its registries on every recipe type in 26.1). The lines before want both in their own forms
before those two bodies match again. `LeadedGlassPlacementGameTest.describe` reads a state through its properties, which
every line has, since `BlockState#getValues` is a stream from 26.1.

Two tests are 1.21.11's and newer's own, since what they test is new in 1.21.11: `nautilus_armor_fits_the_nautilus` and
`lead_spear_is_a_spear`, with `TestCompat.NAUTILUS` and `canBeEquippedBy`. The gear tests (`LeadGearGameTest.gear()`)
take the spear and the nautilus armor from `TestCompat.newerGear()`, which the lines before 1.21.11 want as an empty list.
One is 26.x's own: `smith_pools_include_lead_trade`, which checks that each smith's level 2 trade set draws the lead trade.

## This line

- Uploads are tagged 26.1.2 only, on purpose: the NeoForge builds for 26.1 and 26.1.1 lack the `LootModifier` priority argument this mod compiles against, so the jar would fail there (see `gradle.properties`).
- Java 25, no mappings and no Parchment (Mojang names are the runtime names). `org.gradle.java.home` is pinned in `gradle.properties` to a JDK on this machine; if the daemon fails to start, check that path first.
- Datagen: `:neoforge:runData`. Output: `common/src/generated/resources`, never hand-edited.
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`; its test environment is a `TestEnvironmentDefinition<?>` from 26.1. NeoForge's registers wherever `RegisterGameTestsEvent` comes, the dedicated server included. Fabric's game-test world runs with the Villager Trade Rebalance experiment on, NeoForge's without.
- 13 play scenarios in `scenarios/`, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- Particles are 1.21.9's: the mod's own (`LeadSpeckParticle`) are `SingleQuadParticle`s given their sprite as they are made, and only a `SingleQuadParticle` takes a colour, so the purple smoke and fumes tint those. Fabric API registers their providers through `ParticleProviderRegistry` from 26.1.
- Sprites the mod makes as an atlas loads (`MurkyWater`, `LeadWaterParticles`) are `SpriteSource.DiscardableLoader`s, and a made `SpriteContents` takes the source's texture section besides its animation. Contaminated water's textures are added to the blocks atlas (`atlases/blocks.json`); the lead trim palettes are the items atlas's (`atlases/items.json`).
- 26.1 draws a fluid from a `FluidModel`, one for all the water, so contaminated water gets its murky textures from each loader's `FluidRendererMixin`, which swaps the model `FluidRenderer.tesselate` looks up for a copy with the murky sprites; NeoForge's `FluidModel` carries a tint source and a custom renderer of its own, so each loader makes the copy itself. Its tint still comes through `BiomeColors.getAverageWaterColor` (`BiomeColorsMixin`), and the murk under water through `FogRendererMixin`, which writes into the fog colour `computeFogColor` is handed from 26.1. Fabric registers no water render handler of its own, which up to 1.21.11 made Sodium blend the tint: 26.1's Fabric API takes a model with one. Sodium draws fluids itself, and this line has no Sodium in its run folders, so neither the murky textures nor the tint's blending has been seen under Sodium or Iris on 26.1.
- Villager trades are data (`ModVillagerTradeProvider`): the lead trade per smith, gated by `enable_villager_trades`, in each smith's `minecraft:<smith>/level_2` tag. The Villager Trade Rebalance experiment's datapack replaces `minecraft:armorer/level_2` after every mod's data, so the armorer's level 2 trade set (`data/minecraft/trade_set/armorer/level_2.json`, vanilla's but for its tag) draws from `theleadage:armorer/level_2`, the vanilla tag and the lead trade.
- 1.21.11 content of its own: the Lead Spear (dismount threshold 9.0, as 26.1's netherite spear) and the Lead Nautilus Armor (with their recipes, tags, loot and nugget smelting), spears in the mob gear and chest swaps, parched in `can_wear_lead_armor`, and the `c:armors/horse` and `c:armors/nautilus` tags.
- JEI's plugin (`compat/jei`, JEI 29.6.2.31, as The Silver Age's 26.1 line) shows the leaded glass recipes, info entries and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- Fabric's config screen opens from Mod Menu (`TheLeadAgeModMenu`, through Cloth Config's `AutoConfigClient` from 26.1), compiled against Mod Menu 18.0.2, its newest for 26.1.x. Farmer's Delight Refabricated isn't a dependency here: the Lead Knife compiles against a stub in `fabric/src/main/java/vectorwing`, kept out of every jar.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`, which leaves out `26.1` and `26.1.1` of the rung.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
