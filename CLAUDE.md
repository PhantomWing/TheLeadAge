@~/Documents/Projects/Minecraft/MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age - `version/1.20`

**This file describes the `version/1.20` line**: Minecraft 1.20.1, Fabric and Forge (the Forge jar also runs on NeoForge 1.20.1).
Built with Architectury Loom (`dev.architectury.loom`) with Mojang mappings layered with Parchment; `remapJar` is the shipped jar.
Branched from `version/1.21` on 2026-11-08, at 1.3.0, as The Silver Age's `version/1.20` was: the first 1.20.1 line of the mod.

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

On 1.20.1 that takes more than elsewhere. A class this line hasn't got is never named in a body: the bodies hold what
`TestCompat.craftingInput`, `singleInput` and `recipeFor` give in a `var`, and 1.20.1's stand-ins for 1.21's
`CraftingInput`, `SingleRecipeInput` and `RecipeInput` and 1.20.2's `RecipeHolder` are nested classes of this line's
`TestCompat`. A mock player in the level is `TestLoader.mockServerPlayer` on every line (below), and the bulb's state is
`BlockStateProperties.LIT`. `armorers_buy_lead_in_rebalanced_worlds` and `trial_vaults_give_lead_ingots` skip
themselves here, where `rebalancedApprenticeArmorerBuys` and `trialVaultRewards` are empty. `trimPalette` reads the
palette off the texture a trim draws with (`ArmorTrim.innerTexture`), since the one a trim material names is only ever
vanilla armour's.

## This line

- Java 17 bytecode, built on JDK 21: Architectury Loom 1.7 needs the daemon on 21, so `org.gradle.java.home` is pinned in `gradle.properties` to a JDK on this machine; if the daemon fails to start, check that path first. Mojang mappings with Parchment; `remapJar` is the shipped jar.
- Forge 47.4.20 (the `forge` subproject, `loom.platform=forge` in its `gradle.properties`) with Architectury API 9.2.14, Fabric Loader 0.16.10 with Fabric API 0.92.2. Forge's access transformer is converted from the common access widener (`convertAccessWideners`), and its mixin config is named in `loom.forge` with a refmap of its own. Forge 47 bundles no MixinExtras, so the Forge jar carries it (jar-in-jar).
- Datagen: `:forge:runData`. Output: `common/src/generated/resources`, never hand-edited, in 1.20.1's plural folders (`recipes`, `loot_tables`, `advancements`, `tags/items`, `tags/blocks`). Datagen writes only what it makes: delete the folder before a run that changes the folder layout, or the old files stay.
- Tags are Forge's: `CommonTags` holds the `forge:` ones (`forge:ingots/lead`), and `FabricTags` their Fabric twins (`c:lead_ingots`), which the Fabric recipes and the `c:` lang keys name. A recipe both loaders load names vanilla's items rather than `forge:rods/wooden` or `forge:string`, which Fabric hasn't got. The dyed leaded glass pack puts a dye mod's colours in `forge:stained_glass`, which 1.20.1's datagen puts vanilla's sixteen in, since 1.21's `c:dyed` doesn't exist.
- 1.20.1 has no data components: a leaded glass design rides in the stack's tag, under `theleadage:leaded_glass_config` and `theleadage:leaded_glass_door_config` (`ModDataComponents.StackData`, written through each config's codec). Recipes take the whole `CraftingContainer`, which `recipe/CraftingInput` trims as 1.21's input is trimmed, and the coloured pane's stonecutter recipe has a serializer of its own, since vanilla's `SingleItemRecipe.Serializer` writes no NBT. The networking is Architectury's `NetworkManager` with the payloads as records that read and write themselves.
- Content 1.20.1 hasn't got: the Lead Bulb and Lead Grate are blocks of the mod's own (`LeadBulbBlock` is 1.21's copper bulb rebuilt), on `METAL` sounds, the bulb clicking as a lever does; leaded glass doors use iron door sounds and the weight lands with the anvil's. No Heavy Core (its config option is gone too), no trial chamber loot, no bolt or flow trim, no trade rebalance, no `mob_interactable_doors` tag. Lead armour is an `ArmorMaterial` enum with its modifiers keyed by UUID, there is no gravity attribute (`GravityPlatform`: Forge's `ENTITY_GRAVITY`, Fabric's `LivingEntityMixin` on `travel`'s 0.08), and the Lead Horse Armor's knockback resistance is put on the horse as it is equipped (`LeadHorseArmorItem.equipmentChanged`). 1.20.1 keys a trim material's darker palettes by vanilla's `ArmorMaterials` alone, and an access transformer can't change that, as the check is in `ArmorTrim.getColorPaletteSuffix`'s body: each loader's `ArmorTrimMixin` (not client-only, so the game-test server has it) asks `TrimPalettes` first, which gives lead trim on lead armour `lead_darker`, as 1.21 does. The icons take it from datagen, `<piece>_lead_darker_trim`, its sprites from the blocks atlas's permutations.
- The Lead Spear and Lead Nautilus Armor are always registered and shown only with Vanilla Backport (`BackportPlatform`), whose spear and nautilus armour they are made as; without it each is a plain item. Backported Spears has no 1.20.1 build. The dev runs carry Vanilla Backport on Forge only.
- Game tests in `common/src/gametest`, run on both loaders: `:forge:runGameTest` and `:fabric:runGameTest`, 102 each, two of which skip themselves for what 1.20.1 hasn't got: the rebalanced armorer and the trial vaults. Forge's `TestLoader.mockServerPlayer` is a plain `ServerPlayer` added to the level: Forge 47's `placeNewPlayer` wants a channel the mock connection hasn't got, and a Forge `FakePlayer` never earns an advancement. Each run deletes `run/gametest/world` first, since 1.20.1's game-test server keeps it, and Forge's run writes `level-type=minecraft\:flat` into `run/gametest/server.properties`, since Forge makes the test world from that file rather than vanilla's flat test world.
- 14 play scenarios in `scenarios/`, one of them Fabric's alone, and 1 in `scenarios/server`, whose config for each loader is in `scenarios/server/forge` and `scenarios/server/fabric`. Item stacks in them are written in NBT (`{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}`), not 1.21's components.
- Mekanism (Forge only) has lead of its own, whose processing takes this mod's ore and raw lead through `forge:ores/lead` and `forge:raw_materials/lead`; `resourcepacks/mekanism_lead` in the Forge resources is a built-in pack, off until turned on, listed only with Mekanism, of Mekanism's lead items in this mod's textures. Create, Farmer's Delight and Yori3o's Grappling Hooks (Forge only, data alone in `common/src/main/resources/yo_hooks`) load in Forge's dev runs; Fabric's carry none of them.
- Worn lead armour draws on Fabric through `HumanoidArmorLayerMixin`: the material is named `theleadage:lead`, which
  1.20.1's `getArmorLocation` puts whole into a path in Minecraft's namespace; Forge resolves the namespace itself.
- Three config options for modpacks, as on 1.21.1: `enable_lead_gear`, `enable_creative_tab` and `enable_vanilla_creative_tabs`. There is no config screen on Forge.
- Published with `publishMods` from `fabric/build.gradle` and `forge/build.gradle`, with the `-PpublishDryRun` flag. Both uploads are tagged from `supported_minecraft_versions`, 1.20.1, and the Forge one lists Forge and NeoForge as its modloaders: NeoForge 1.20.1 runs Forge 47 mods.
- GitHub Actions: `release.yml`.
- Hand-authored access widener: `common/src/main/resources/theleadage.accesswidener`, which Forge's build converts. A first suspect when a port fails to load.
