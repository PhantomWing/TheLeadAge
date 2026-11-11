@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/26.2`

**This file describes the `version/26.2` line**: Minecraft 26.2, Fabric and NeoForge.
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

The bodies are the same on every line, imports included: a mob a test spawns, an id and a trim are held in a `var`,
since their classes moved package or were renamed (1.21.2's trims, 1.21.11's mobs and `Identifier`), and every call
whose shape differs goes through `TestCompat`. A test of something a line doesn't have skips itself through
`TestCompat`: `nautilus_armor_fits_the_nautilus` and `lead_spear_is_a_spear` where `leadNautilusArmor()` and
`leadSpear()` are empty, before 1.21.11; `leaded_glass_shatters_only_under_a_player` where `shownAs` is, before 26.3,
whose `Level#destroyBlock` first reaches the glass's destroy hook with no breaker; and
`lead_knife_fallback_keeps_sword_properties` where Farmer's Delight is loaded. The gear tests take the spear and the
nautilus armour from `newerGear()`, empty before 1.21.11. `smith_pools_include_lead_trade` and
`armorers_buy_lead_in_rebalanced_worlds` ask `TestCompat` about the trades, listings in `VillagerTrades` up to 1.21.11
and trade sets from 26.1; the vein tests read the veins through `leadVeins` and their place in a chunk's generation
through `leadVeinsOutOfPlace`, a NoiseChunk's fillers up to 26.2 and the noise settings' material rule from 26.3. So
the gametest folder, `TestCompat` aside, diffs clean against every other line's.

## This line

- Java 25, no mappings and no Parchment (Mojang names are the runtime names). `org.gradle.java.home` is pinned in `gradle.properties` to a JDK on this machine; if the daemon fails to start, check that path first.
- Datagen: `:neoforge:runData`. Output: `common/src/generated/resources`, never hand-edited.
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`; its test environment is a `TestEnvironmentDefinition<?>` from 26.1. NeoForge's registers wherever `RegisterGameTestsEvent` comes, the dedicated server included. Fabric's game-test world runs with the Villager Trade Rebalance experiment on, NeoForge's without.
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- Particles are 1.21.9's: the mod's own (`LeadSpeckParticle`) are `SingleQuadParticle`s given their sprite as they are made, and only a `SingleQuadParticle` takes a colour, so the purple smoke and fumes tint those. Fabric API registers their providers through `ParticleProviderRegistry` from 26.1.
- Sprites the mod makes as an atlas loads (`MurkyWater`, `LeadWaterParticles`) are `SpriteSource.DiscardableLoader`s, and a made `SpriteContents` takes the source's texture section besides its animation. Contaminated water's textures are added to the blocks atlas (`atlases/blocks.json`); the lead trim palettes are the items atlas's (`atlases/items.json`).
- 26.1 draws a fluid from a `FluidModel`, one for all the water, so contaminated water gets its murky textures from each loader's `FluidRendererMixin`, which swaps the model `FluidRenderer.tesselate` looks up for a copy with the murky sprites; NeoForge's `FluidModel` carries a tint source and a custom renderer of its own, so each loader makes the copy itself. Its tint still comes through `BiomeColors.getAverageWaterColor` (`BiomeColorsMixin`), and the murk under water through `FogRendererMixin`, which writes into the fog colour `computeFogColor` is handed from 26.1. Fabric registers no water render handler of its own, which up to 1.21.11 made Sodium blend the tint: 26.1's Fabric API takes a model with one. Sodium draws fluids itself, and this line has no Sodium in its run folders, so neither the murky textures nor the tint's blending has been seen under Sodium or Iris on 26.x.
- Villager trades are data (`ModVillagerTradeProvider`): the lead trade per smith, gated by `enable_villager_trades`, in each smith's `minecraft:<smith>/level_2` tag. The Villager Trade Rebalance experiment's datapack replaces `minecraft:armorer/level_2` after every mod's data, so the armorer's level 2 trade set (`data/minecraft/trade_set/armorer/level_2.json`, vanilla's but for its tag) draws from `theleadage:armorer/level_2`, the vanilla tag and the lead trade.
- 1.21.11 content of its own: the Lead Spear (dismount threshold 9.0, as 26.x's netherite spear) and the Lead Nautilus Armor (with their recipes, tags, loot and nugget smelting), spears in the mob gear and chest swaps, parched in `can_wear_lead_armor`, and the `c:armors/horse` and `c:armors/nautilus` tags.
- 26.2 moved the entity type constants to `EntityTypes`, gathered the copper blocks into `WeatheringCopperCollection`s and the dyed variants into `ColorCollection`s (`Items.DYE.pick(DyeColor.RED)`), and dropped the `ItemTags` constants of the tags mirroring a block family; the tests reach those through `TestCompat`, and datagen's tag providers add values through `ValueAppender`, since a `TagAppender` takes a `ResourceKey` from 26.2. `LevelRenderer`'s dirty marking (`allChanged`, `setBlocksDirty`, `blockChanged`) is `LevelExtractor`'s (`Minecraft.levelExtractor`) from 26.2, and a block change reaches it without its states, so the water around raw lead is drawn again from each loader's `ClientLevelMixin`, at the head of `ClientLevel.sendBlockUpdated`.
- JEI's plugin (`compat/jei`, JEI 30.35.0.223) shows the leaded glass recipes, info entries and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- Fabric's config screen opens from Mod Menu (`TheLeadAgeModMenu`, through Cloth Config's `AutoConfigClient` from 26.1), compiled against Mod Menu 20.0.3, its newest for 26.2. Farmer's Delight Refabricated isn't a dependency here: the Lead Knife compiles against a stub in `fabric/src/main/java/vectorwing`, kept out of every jar.
- 1.3.0's compatibility and modpack options (2026-11-11), from the 1.21.1 line and The Silver Age's 26.2 line:
  - Not on this line: Backported Spears and Vanilla Backport add nothing on 26.2, whose spears and nautilus armour are
    vanilla's and this line's own Lead Spear and Lead Nautilus Armor (with their chest swaps, zombies' spears and parched
    in `can_wear_lead_armor`) were here already; the bars, chains and lanterns tags Copper Age Backport fills on 1.21.1
    are vanilla's own here, which lead's blocks were already in. Mekanism (and so its resource pack), EMI, NeoForge's
    Create and NeoForge's Farmer's Delight have no 26.2 build.
  - Sulfur Cubes, vanilla's on 26.2, swallow lead's ore, raw block, block, cut and chiseled lead and bulb
    (`minecraft:sulfur_cube_archetype/slow_flat`). `weapon_attributes/lead_spear.json` gives the Lead Spear Better
    Combat's spear swings.
  - Three config options for modpacks: `enable_lead_gear` takes away the gear in `ModItems.isLeadGear`, the spear and
    nautilus armour among it - recipes through their condition, loot (`LeadLootSpec.itemEnabled`), mobs wearing it
    (folded into `mobLeadEquipment()`), the creative tabs - and `enable_creative_tab` and `enable_vanilla_creative_tabs`
    pick the tabs (`ui/VanillaTabs`, each item after its iron or copper counterpart, after the last of the copper age's
    `WeatheringCopperCollection`s, filled through `platform/CreativeTabPlatform`: NeoForge's mod bus, and a Fabric
    `CreativeModeTabEvents` phase after the default one). All three are read from each side's own config. The knife goes
    in Farmer's Delight's tab after its Iron Knife, the sheet in Create Fly's `create:base` after its Iron Sheet. The armour
    material is made as the armour registers (`ModArmorMaterials.LEAD_ARMOR_MATERIAL`, a memoized supplier), after the
    sounds, since its equip sound is the mod's own and an Architectury 21 `RegistrySupplier` is no longer a holder.
  - JEI shows `compat/LeadInfo`'s info pages for the pressure plate and lead light. The Lead Bulb (`LeadBulbBlock`), lead
    armour, the Lead Weight and leaded glass panes play sound events of their own (`ModSounds`), each vanilla's sound under
    the mod's name.
  - Yori3o's Grappling Hooks' Lead Grappling Hook is data the mod reads (`yo_hooks/hooks.json`, its tags, recipes, models
    and textures under `yo_hooks`); 5.4.0 reads every mod's hooks.json on both loaders. Its `items/` definition names the
    mod's own `yo_hooks:hook_active` property, which fails to parse without it, so it is in a pack of its own
    (`resourcepacks/lead_grappling_hook`), turned on only with Grappling Hooks (`GrapplingHookPack` on NeoForge, a
    built-in pack in `TheLeadAgeFabricClient`). The dev runs load Grappling Hooks 5.4.0 and YACL 3.9.7
    (`enable_grappling_hooks`), but for the game tests, datagen and NeoForge's server scenarios, whose client the mods'
    warnings screen holds before the title screen.
- **Create Fly** (Fabric's Create, mod id `create`) gets 1.21.1's Create recipes in its own format, which isn't Create's
  (one `ingredient`, a `results` list): `neoforge/.../datagen/CreateFlyRecipeProvider` writes them as JSON into
  `fabric/src/generated/resources`, which only the Fabric jar carries (`fabric/build.gradle`'s `main` resources; the data
  run passes the folder as `theleadage.datagen.fabricOutput`), each under `fabric:load_conditions` alone: the Press makes
  Lead Sheets, Crushed Raw Lead (`create:crushed_raw_lead`) is washed into 9 nuggets and smelted or blasted into an
  ingot, and the ore, raw lead and its block are crushed, which Create Fly leaves out. The Fabric dev runs load Create
  Fly 26.2-6.0.9-2, a beta (`enable_create_fly`), the game tests too, but for the portal's server scenarios
  (`fabric/build.gradle` knows them by their init scripts). `common/src/generated` holds no Create recipes: 1.21.1's, in
  Create's format, which Create Fly fails to parse, were left there by earlier ports and removed.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
