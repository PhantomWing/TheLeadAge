@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.21.10`

**This file describes the `version/1.21.10` line**: Minecraft 1.21.10 (the jar covers 1.21.9 and 1.21.10), Fabric and NeoForge.
Built with Architectury Loom (`dev.architectury.loom`) with Mojang mappings layered with Parchment; `remapJar` is the shipped jar.

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

- Java 21, Mojang mappings with Parchment. `org.gradle.java.home` is pinned in `gradle.properties` to a JDK on this machine; if the daemon fails to start, check that path first.
- Datagen: `:neoforge:runData`. Output: `common/src/generated/resources`, never hand-edited.
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). From 1.21.5 Fabric API's own registration takes one annotated method per test, so Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`. NeoForge 21.10 posts `RegisterGameTestsEvent` only while the registries load, before they freeze, so NeoForge's registers wherever it comes, the dedicated server included.
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- Particles are 1.21.9's: the mod's own (`LeadSpeckParticle`) are `SingleQuadParticle`s given their sprite as they are made, and only a `SingleQuadParticle` takes a colour, so the purple smoke and fumes tint those. NeoForge's dev client needs no `-Dneoforge.warnings.onlyin.hide`, as 1.21.8's does: Architectury API 18 has no `@OnlyIn`, so NeoForge's warning screen about it doesn't come up.
- JEI's plugin (`compat/jei`, JEI 26.3) shows the leaded glass recipes, info entries and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- 1.3.0's compatibility and modpack options (2026-11-11), from the 1.21.1 line and The Silver Age's 1.21.10 line:
  - The Lead Spear is 1.21.11's, which Backported Spears (`spears`) backports to this version. It is always registered,
    so a world keeps it, and only shown in the creative tabs with Backported Spears (`ModItems`, `BackportPlatform`):
    with it the spear is made by its `Spears.registerSpearRaw(id, material, ...)`, which builds the item without
    registering it, with the 1.21.11 line's numbers (`compat/LeadSpearStats`); without it a plain item. Its recipe and
    smelting load only with the mod, and it is in `minecraft:spears` (`BackportTags`), which Backported Spears puts into
    the enchantable tags. Its `items/` definition draws the flat sprite in the GUI, on the ground and in a frame and
    `lead_spear_in_hand` otherwise (`ModModelProvider.spear`), the held model's parent the mod's own `spear_in_hand`.
    `weapon_attributes/lead_spear.json` gives it Better Combat's spear swings. NeoForge's dev runs load Backported Spears
    1.0.1 (`enable_backported_spears`), but for the game tests and datagen. Its Fabric jar is built with Loom 1.17, which
    this rung's Loom 1.14 refuses to remap, and asks for Fabric Loader 0.19.5, so Fabric compiles against a stub of its
    `Spears` (`fabric/src/main/java/com/notunanancyowen/spears`, left out of the jar) and its dev runs go without it; on
    Fabric the spear is only a spear where Backported Spears registered its components first (`suggests` orders it).
  - Not on this line: Vanilla Backport (the nautilus armour, its chest swaps, zombies' spears, Sulfur Cubes, Parched),
    Mekanism and its resource pack, and EMI have no 1.21.9 or 1.21.10 build; Yori3o's Grappling Hooks' 1.21.9-1.21.10
    builds read no `yo_hooks/hooks.json`, so there is no Lead Grappling Hook; and the bars, chains and lanterns tags
    Copper Age Backport fills on 1.21.1 are vanilla's own here, which lead's blocks were already in.
  - Three config options for modpacks: `enable_lead_gear` takes away the gear in `ModItems.isLeadGear` - recipes through
    their condition, loot (`LeadLootSpec.itemEnabled`), mobs wearing it (folded into `mobLeadEquipment()`), the creative
    tabs - and `enable_creative_tab` and `enable_vanilla_creative_tabs` pick the tabs (`ui/VanillaTabs`, each item after
    its iron or copper counterpart, after the last of the copper age's families from 1.21.9, filled through
    `platform/CreativeTabPlatform`: NeoForge's mod bus, which `neoforge.mods.toml` orders after Backported Spears, and a
    Fabric phase after the default one). All three are read from each side's own config. The knife goes in Farmer's
    Delight's tab after its Iron Knife, the sheet in Create Fly's `create:base` after its Iron Sheet.
  - JEI shows `compat/LeadInfo`'s info pages for the pressure plate and lead light. The Lead Bulb, lead armour, the Lead
    Weight and leaded glass panes play sound events of their own (`ModSounds`), each vanilla's sound under the mod's name.
- **Create Fly** (Fabric's Create, mod id `create`) gets 1.21.1's Create recipes in its own format, which isn't Create's
  (one `ingredient`): `neoforge/.../datagen/CreateFlyRecipeProvider` writes them as JSON into
  `fabric/src/generated/resources`, which only the Fabric jar carries (`fabric/build.gradle`'s `main` resources; the data
  run passes the folder as `theleadage.datagen.fabricOutput`), each under `fabric:load_conditions` alone: the Press makes
  Lead Sheets (`result` and `results` both, for every Create Fly build), Crushed Raw Lead (`create:crushed_raw_lead`) is
  washed into 9 nuggets and smelted or blasted into an ingot, and the ore, raw lead and its block are crushed, which
  Create Fly leaves out. The Fabric dev runs load Create Fly 1.21.10-6.0.9-1 (`enable_create_fly`), the game tests too,
  but for the portal's server scenarios (`fabric/build.gradle` knows them by their init scripts); it needs Fabric Loader
  0.17.3, so the dev runs use `fabric_loader_dev_version` while the manifest keeps `fabric_loader_version`.
  `common/src/generated` holds no Create recipes: 1.21.1's, in Create's format, which Create Fly fails to parse, were left
  there by earlier ports and removed. Create for NeoForge has no 1.21.10 build.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
