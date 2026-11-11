@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.21.11`

**This file describes the `version/1.21.11` line**: Minecraft 1.21.11, Fabric and NeoForge.
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
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). From 1.21.5 Fabric API's own registration takes one annotated method per test, so Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`. NeoForge 21.10 and 21.11 post `RegisterGameTestsEvent` only while the registries load, before they freeze, so NeoForge's registers wherever it comes, the dedicated server included.
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- Particles are 1.21.9's: the mod's own (`LeadSpeckParticle`) are `SingleQuadParticle`s given their sprite as they are made, and only a `SingleQuadParticle` takes a colour, so the purple smoke and fumes tint those. NeoForge's dev client needs no `-Dneoforge.warnings.onlyin.hide`, as 1.21.8's does: Architectury API 19 has no `@OnlyIn`, so NeoForge's warning screen about it doesn't come up.
- Sprites the mod makes as an atlas loads (`MurkyWater`, `LeadWaterParticles`) are 1.21.11's `SpriteSource.DiscardableLoader`s, and a made `SpriteContents` takes the source's texture section (blur, clamp, mipmaps) besides its animation, as vanilla's loader gives it. Contaminated water's textures are added to the blocks atlas (`atlases/blocks.json`); the lead trim palettes are the items atlas's from 1.21.11 (`atlases/items.json`).
- 1.21.11 content of its own: the Lead Spear and the Lead Nautilus Armor (with their recipes, tags, loot and nugget smelting), spears in the mob gear and chest swaps, parched in `can_wear_lead_armor`, and the `c:armors/horse` and `c:armors/nautilus` tags.
- JEI's plugin (`compat/jei`, JEI 27.6, pinned: newer builds use a Loom this line's refuses) shows the leaded glass recipes, info entries and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- Fabric's config screen opens from Mod Menu (`TheLeadAgeModMenu`), compiled against Mod Menu 17.0.1, its newest for 1.21.11. Farmer's Delight Refabricated can't be on the classpath here (its 1.21.11 builds' classTweaker v2 breaks this Loom), so the Lead Knife compiles against a stub in `fabric/src/main/java/vectorwing`, kept out of every jar.
- 1.3.0's compatibility and modpack options (2026-11-11), from the 1.21.1 line and The Silver Age's 1.21.11 line:
  - Three config options for modpacks: `enable_lead_gear` takes away the gear in `ModItems.isLeadGear` (the tools,
    sword, knife, spear, armour, horse and nautilus armour) - recipes through their condition, loot
    (`LeadLootSpec.itemEnabled`), mobs wearing it (folded into `mobLeadEquipment()`), the creative tabs - and
    `enable_creative_tab` and `enable_vanilla_creative_tabs` pick the tabs (`ui/VanillaTabs`, each item after its iron
    or copper counterpart, after the last of the copper age's families, the spear after the Iron Spear and the nautilus
    armour after the Iron Nautilus Armor, filled through `platform/CreativeTabPlatform`: NeoForge's mod bus and a Fabric
    phase after the default one, so another mod's anchors are there to insert after). All three are read from each
    side's own config. The knife goes in Farmer's Delight's tab after its Iron Knife, the sheet in Create Fly's
    `create:base` after its Iron Sheet; Farmer's Delight Refabricated can't be in the dev runs here (above), so the
    knife's place is unchecked.
  - JEI shows `compat/LeadInfo`'s info pages for the pressure plate and lead light. The Lead Bulb, lead armour, the Lead
    Weight and leaded glass panes play sound events of their own (`ModSounds`), each vanilla's sound under the mod's name.
  - Yori3o's Grappling Hooks reads every jar's `yo_hooks/hooks.json`, so the Lead Grappling Hook is data alone, in
    `common/src/main/resources`: the hook, its `yo_hooks` models and 1.21.11 `items/` definitions (`yo_hooks:hook_active`
    picks the handle), textures, names, tags, and its recipes, hand-written in `data/theleadage/recipe` behind both
    loaders' mod-loaded conditions and `enable_lead_gear`. `enable_grappling_hooks` (off by default) loads 4.5.2 in both
    loaders' dev runs, as The Silver Age's 1.21.11 line does: on NeoForge 4.5.2 registers none of its hooks in a dev run,
    so no world opens, and on both it logs an ERROR on every start, which fails the load check.
  - `weapon_attributes/lead_spear.json` gives the spear Better Combat's spear swings.
  - Not on this line: Backported Spears and Vanilla Backport only add what 1.21.11 has (the spear, nautilus armour,
    their chest swaps, zombies' spears, Parched), and Vanilla Backport, Mekanism (and so its resource pack) and EMI
    have no 1.21.11 build, so no Sulfur Cube tag; the bars, chains and lanterns tags Copper Age Backport fills on 1.21.1
    are vanilla's own here, which lead's blocks were already in.
- **Create Fly** (Fabric's Create, mod id `create`) gets 1.21.1's Create recipes in its own format, which isn't Create's
  (one `ingredient`): `neoforge/.../datagen/CreateFlyRecipeProvider` writes them as JSON into
  `fabric/src/generated/resources`, which only the Fabric jar carries (`fabric/build.gradle`'s `main` resources; the data
  run passes the folder as `theleadage.datagen.fabricOutput`), each under `fabric:load_conditions` alone: the Press makes
  Lead Sheets (`result` and `results` both: this rung's 6.0.9-2 reads the one stack, 6.0.9-3 and newer the list), Crushed
  Raw Lead (`create:crushed_raw_lead`) is washed into 9 nuggets and smelted or blasted into an ingot, and the ore, raw
  lead and its block are crushed, which Create Fly leaves out. The Fabric dev runs load Create Fly 1.21.11-6.0.9-2
  (`enable_create_fly`; 6.0.9-3 and newer are built with Loom 1.15, which this Loom refuses), the game tests too, but for
  the portal's server scenarios (`fabric/build.gradle` knows them by their init scripts). `common/src/generated` holds no
  Create recipes: 1.21.1's, in Create's format, which Create Fly fails to parse, were left there by earlier ports and
  removed. Create for NeoForge has no 1.21.11 build.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
