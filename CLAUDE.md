@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.21`

**This file describes the `version/1.21` line**: Minecraft 1.21.1 (the jar covers 1.21 and 1.21.1), Fabric and NeoForge. A **baseline** line: new features start here.
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
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too").
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. The Fabric upload is tagged from `supported_minecraft_versions`, 1.21 and 1.21.1, and the NeoForge one from `neoforge_minecraft_versions`, 1.21.1 alone, since NeoForge 21.1 exists for 1.21.1 only. The Fabric manifest asks for `fabric-api >=0.102.0`, the newest for 1.21, which the Fabric code compiled against on 2026-11-03: a change that needs a newer Fabric API raises that floor and drops 1.21 from the list.
- Newer vanilla content that mods backport to 1.21.1, under vanilla's own ids, as The Silver Age's 1.21.1 line has it (1.3.0):
  - The Lead Spear and Lead Nautilus Armor are always registered, so a world keeps them, and only shown in the
    creative tabs with the mod that backports them (`ModItems`, `BackportPlatform`). With Backported Spears (`spears`)
    the spear is made by its `Spears.registerSpearRaw`, with Vanilla Backport by its `SpearItem`, both with the 1.21.11
    line's numbers (`compat/LeadSpearStats`); with Vanilla Backport the armour is its `NautilusArmorItem`, with the
    Lead Horse Armor's knockback resistance, whose renderer reads `theleadage:textures/entity/nautilus/armor/lead.png`
    (a copy of `equipment/nautilus_body/lead.png`). Without them each is a plain item. The two mods crash a client
    holding a spear when both are installed, so NeoForge's game runs take Backported Spears and its datagen Vanilla
    Backport (whose iron nautilus armour and spear the loot modifiers name), and Fabric's runs Vanilla Backport alone.
  - Vanilla Backport draws only its own spears held as spears, so the Lead Spear's held model is registered with its
    Platform library in each loader's client setup. Its recipe and smelting load with either mod (`neoforge:or`, which
    `FabricConditionsProvider` turns into `fabric:or`). `weapon_attributes/lead_spear.json` gives it Better Combat's
    spear swings.
  - Lead Nautilus Armor takes the 1.21.11 line's six ocean chest swaps from Vanilla Backport's iron nautilus armour, and
    the Lead Spear its two from the iron spear (`LeadLootSpec`, each with `requiredMods`, so its loot modifier loads, and
    Fabric's mixin applies it, only with Vanilla Backport). A zombie's iron spear can turn into it as its sword does.
  - Sulfur Cubes swallow lead's blocks, ores, cut and chiseled lead and the bulb (`minecraft:sulfur_cube_archetype/slow_flat`);
    Parched are in `can_wear_lead_armor`, optional; `BackportTags` holds the 1.21.9 `bars`, `chains`, `lanterns` and
    `spears` tags Vanilla Backport and Copper Age Backport fill.
  - Yori3o's Grappling Hooks reads every jar's `yo_hooks/hooks.json`, so the Lead Grappling Hook is data alone, in
    `common/src/main/resources`: 21 durability, 10 long, 4 enchantability and 2 damage on hit, the glass cannon lead's
    tools are (gold's hook is 24 durability, copper's 10 long, diamond's 2 damage); its textures are The Silver Age's
    hook on the Lead Pickaxe's ramp, its recipes hand-written behind the mod-loaded condition and `enable_lead_gear`.
  - Backported Spears needs Fabric Loader 0.18.1 and Grappling Hooks 0.19, past this line's 0.16.14, so both are only
    tried on NeoForge in dev. YACL's jar bundles libraries a dev run doesn't unpack, which `forgeRuntimeLibrary` adds.
- Mekanism (NeoForge only, 10.7) has lead of its own, whose processing takes this mod's ore and raw lead through
  `c:ores/lead` and `c:raw_materials/lead`, so the mod adds no processing items. `resourcepacks/mekanism_lead` in the
  NeoForge resources is a built-in pack, off until turned on (`MekanismLeadPackHandler`), listed only with Mekanism:
  Mekanism's lead ingot, nugget, raw lead, blocks and ores as this mod's textures, and its dust, dirty dust, clump,
  shard and crystal as Mekanism's own lead ones on the Lead Ingot's ramp, blended (`recolor.js --blend`) since they
  have 12 to 31 shades to its 8, the dirty dust's stone kept as it is. Its slurries are tinted in Mekanism's code
  (`PrimaryResource.LEAD`), which a pack can't change.
- Three config options for modpacks: `enable_lead_gear` takes away the gear in `ModItems.isLeadGear` - recipes through
  their condition, loot (`LeadLootSpec.itemEnabled`), mobs wearing it (folded into `mobLeadEquipment()`), the creative
  tabs - and `enable_creative_tab` and `enable_vanilla_creative_tabs` pick the tabs (`ui/VanillaTabs`, each item after
  its iron or copper counterpart, filled through `platform/CreativeTabPlatform` as The Silver Age's are). All three are
  read from each side's own config; unlike Silver's, `enable_lead_gear` isn't sent to clients, since `LeadSettingsPayload`
  is what the game tests compare across lines.
- The Lead Bulb, lead armour, the Lead Weight and leaded glass panes play sound events of their own (`ModSounds`), each
  vanilla's sound under the mod's name, so the subtitles don't name a copper bulb, iron armour, an anvil, a mace or an
  item frame.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
