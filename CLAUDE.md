@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.21.4`

**This file describes the `version/1.21.4` line**: Minecraft 1.21.4, Fabric and NeoForge.
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
- Two config options pick the creative tabs, read from the player's own config: `enable_creative_tab` (the mod's tab,
  hidden by giving it no items) and `enable_vanilla_creative_tabs` (`ui/VanillaTabs`: each item after its counterpart
  in vanilla's tabs, the knife after Farmer's Delight's Iron Knife and the sheet after Create's Iron Sheet, both
  dormant here since neither mod has a 1.21.4 build). The tabs are filled through `CreativeTabPlatform`: on NeoForge
  from the mod's own bus (`BuildCreativeModeTabContentsEvent`), on Fabric in a phase after the default one. Both throw
  on an anchor the tab doesn't list, so `VanillaTabs` can try a list of anchors.
- `enable_lead_gear` takes away the gear in `ModItems.isLeadGear` (the tools, sword, knife, armour and horse armour):
  their recipes and the Lead Grappling Hook's through `theleadage:config_boolean`, their loot
  (`LeadLootSpec.itemEnabled`), the creative tabs, and mobs spawning with it, folded into `mobLeadEquipment()`. It
  isn't synced to clients, as on 1.21.1: the creative tabs follow the player's own config.
- The bulb, armour, Lead Weight and leaded glass panes play sound events of their own (`ModSounds`, `sounds.json`),
  each vanilla's sound under a subtitle that names lead. The armour material takes `ModSounds.ARMOR_EQUIP_LEAD` as its
  equip sound holder, so `ModSounds` registers before the items.
- JEI has a NeoForge build for 1.21.4 (20.0.0.4) and none for Fabric: `compat/jei` is compiled against
  `jei-1.21.4-common-api` and loaded in NeoForge dev runs while `enable_jei` is on (`-Penable_jei=false` leaves it
  out). It shows the leaded glass crafting arrangements, the info pages (`compat/LeadInfo`, `LeadedGlassInfo`) and each
  leaded glass design as its own stack. JEI 20's crafting extension has no `setRecipe`, grid helper or focus links:
  `JeiLeadedGlassExtension.getIngredients` gives each cell's variants as a `SlotDisplay.Composite`, and the result
  comes from the recipe's own `display()`, which `LeadedGlassDisplayRecipe` overrides with a
  `ShapedCraftingRecipeDisplay` (a `CustomRecipe` has none from 1.21.2, and JEI takes the first). Every slot reads one
  shared cycle, so index-aligned variants cycle in step; under a focus only the matched slot is narrowed, so
  `onDisplayedIngredientsUpdate` pins the other cycling slots to its colour, matching stacks by design as JEI does
  (`LeadedGlassSubtypes`). Checked in a play session: the panes around a lead ingot cycle with their result, and the
  Leaded Glass Pane's recipes (R) show clear leaded glass for the clear pane. EMI has no 1.21.4 build.
- Yori3o's Grappling Hooks reads every jar's `yo_hooks/hooks.json` from its Fabric 4.0 on 1.21.4, so the Lead
  Grappling Hook is data alone, in `common/src/main/resources`: the hook (21 durability, 10 long, 4 enchantability),
  its `yo_hooks` models, textures, names and tags, its `items/` definitions in the built-in pack
  `fabric/src/main/resources/resourcepacks/grappling_hooks`, which Fabric's client turns on only with the mod (in
  Fabric's own resources, since in a dev run common's are a mod of their own, which the pack lookup misses), and its recipes, hand-written in
  `data/theleadage/recipe` behind the hook item being registered (`neoforge:registered`, `fabric:registry_contains`)
  and `enable_lead_gear`. Its NeoForge builds for 1.21.4 (to 3.8) have a fixed set of hooks. The Fabric build needs
  Fabric Loader 0.17, past this line's 0.16.14, so no dev run loads it and the hook is untested in the game here.
- Not on this line, since their mods have no 1.21.4 build: 1.3's Lead Spear and Lead Nautilus Armor (Backported Spears,
  Vanilla Backport), the bars, chains, lanterns, spears and Sulfur Cube tags (Vanilla Backport, Copper Age Backport),
  Better Combat's spear (no spear), the Mekanism Lead pack, EMI, Create and Farmer's Delight.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
