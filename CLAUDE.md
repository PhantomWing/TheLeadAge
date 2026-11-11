@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.21.8`

**This file describes the `version/1.21.8` line**: Minecraft 1.21.8 (the jar covers 1.21.7 and 1.21.8), Fabric and NeoForge.
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
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). From 1.21.5 Fabric API's own registration takes one annotated method per test, so Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`; NeoForge's skips a plain dedicated server, which posts `RegisterGameTestsEvent` again with its registries frozen.
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- NeoForge's dev client runs with `-Dneoforge.warnings.onlyin.hide=true`: NeoForge 21.8 stops it on a warnings screen about Architectury's `@OnlyIn`, which held back quick play into a server.
- JEI's plugin (`compat/jei`, JEI 24.2) shows the leaded glass recipes, info entries (`LeadedGlassInfo`, and `compat/LeadInfo`'s pages for the pressure plate and lead light) and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- 1.3's compatibility, as The Silver Age's 1.21.8 line has it. Vanilla Backport, Copper Age Backport and Mekanism have no build for 1.21.7 or 1.21.8, so this line has no Lead Nautilus Armor, no Sulfur Cube, Parched or iron spear loot swaps, no 1.21.9 `bars`, `chains` and `lanterns` tags and no Mekanism pack; NeoForge has no Create.
  - The Lead Spear is always registered, so a world keeps it, and only shown in the creative tabs with Backported Spears (`spears`, `ModItems`, `BackportPlatform`), which makes it with its `Spears.registerSpearRaw(id, ModTiers.LEAD, ...)` and the 1.21.11 line's numbers (`compat/LeadSpearStats`); without it the spear is a plain item. On Fabric Backported Spears registers its components in its own initializer, so the spear is only a spear where it ran first (`suggests`); otherwise it falls back to a plain item and logs a warning. Its `items/` definition draws it flat in the GUI, on the ground and in a frame, and its `_in_hand` model held (`spear_in_hand`, copied into this namespace). Its recipe and smelting load with the mod; it is in `minecraft:spears` (`BackportTags`) and `lead_equipment`, and `weapon_attributes/lead_spear.json` gives it Better Combat's spear swings. Backported Spears' Fabric build asks for Fabric Loader 0.18.4, past the dev runs' 0.17.2, so only NeoForge's dev runs load it.
  - Yori3o's Grappling Hooks 4.5.3 on Fabric reads every mod's `yo_hooks/hooks.json`, so the Lead Grappling Hook is data alone, in `fabric/src/main/resources`: the hook (21 durability, 10 long, 4 enchantability, 2 damage on hit), its `yo_hooks` models, textures, names and tags, and its recipes behind Fabric's mod-loaded condition and `enable_lead_gear`. Its NeoForge build for 1.21.8 (4.5.1, the newest) reads only the first `yo_hooks/hooks.json` on the classpath, which kept its own hooks from registering in The Silver Age, so the NeoForge jar carries none of it. The hook's `items/` definition names Grappling Hooks' `yo_hooks:hook_active` condition, which fails to parse without the mod, so it is in a built-in pack, `resourcepacks/lead_grappling_hook`, that `TheLeadAgeFabricClient` registers only where the mod is loaded.
  - **Create Fly** (Fabric's Create, mod id `create`) gets 1.21.1's Create recipes in its own format, which isn't Create's (one `ingredient`): `neoforge/.../datagen/CreateFlyRecipeProvider` writes the Press's Lead Sheet, Crushed Raw Lead's washing and smelting, and the crushing of the ores, raw lead and its block, which Create Fly leaves out, as JSON into `fabric/src/generated/resources`, which only the Fabric jar carries (`fabric/build.gradle`'s `main` resources; the data run passes the folder as `theleadage.datagen.fabricOutput`), each under `fabric:load_conditions` alone. The Press's result is written as both `result` and `results`, since 6.0.9-2 reads the list and the builds before it one stack. `common/src/generated` holds no Create recipes: 1.21.1's, in Create's format, which Create Fly fails to parse, were left there by earlier ports and removed. The Fabric dev runs load Create Fly 6.0.9-1 (`enable_create_fly`), the game tests too, but not the portal's server scenarios, whose `/reload` with Create Fly held The Silver Age's dev server past the client's timeout; 6.0.9-2 is built with Loom 1.15, which this rung's Loom refuses. It needs Fabric Loader 0.17.2, so the dev runs use `fabric_loader_dev_version` while the manifest keeps `fabric_loader_version`. Create Fly draws Crushed Raw Lead with Create's own texture: the Create override pack is NeoForge's.
  - Three config options for modpacks: `enable_lead_gear` takes away the gear in `ModItems.isLeadGear` - recipes through their condition, loot (`LeadLootSpec.itemEnabled`), mobs wearing it (folded into `mobLeadEquipment()`), the creative tabs - and `enable_creative_tab` and `enable_vanilla_creative_tabs` pick the tabs (`ui/VanillaTabs`, each item after its iron or copper counterpart, the knife after Farmer's Delight's Iron Knife and the sheet after Create's Iron Sheet in their own tabs, filled through `platform/CreativeTabPlatform`: NeoForge's mod bus, after Backported Spears through `neoforge.mods.toml`, and Fabric's phase after the default one). All three are read from each side's own config; `enable_lead_gear` isn't sent to clients, since `LeadSettingsPayload` is what the game tests compare across lines.
  - The Lead Bulb, lead armour, the Lead Weight and leaded glass panes play sound events of their own (`ModSounds`), each vanilla's sound under the mod's name, so the subtitles don't name a copper bulb, iron armour, an anvil, a mace or an item frame.
  - Each optional mod the dev runs load has a switch of its own in `gradle.properties`, `enable_<mod>` (Create Fly and Grappling Hooks on Fabric, Backported Spears on NeoForge).
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
