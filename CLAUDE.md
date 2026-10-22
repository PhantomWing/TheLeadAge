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

The test bodies are 1.21.1's but for seven calls the 1.2 port moved into `TestCompat`: six on 1.21.3 (`trim`,
`trimPalette`, `hasRecipe`, `recipesFor`, `use`, `nearestDirection`), which `version/1.21`'s bodies still make
directly, and `assertTrue` on 1.21.5, since `GameTestHelper.assertTrue` takes only a Component from 1.21.5 to
1.21.10, which the bodies of `version/1.21`, `version/1.21.3` and `version/1.21.4` still call on the helper. Their
`TestCompat`s want those methods in their own forms before the bodies match again. On 1.21.11 an eighth, `offer`:
`ItemListing.getOffer` takes the trader's level first from 1.21.11, and the bodies of the lines before still call it
on the listing.

Two tests are this line's own, since what they test is new in 1.21.11: `nautilus_armor_fits_the_nautilus` and
`lead_spear_is_a_spear`, with `TestCompat.NAUTILUS` and `canBeEquippedBy`. The gear tests (`LeadGearGameTest.gear()`)
take the spear and the nautilus armor from `TestCompat.newerGear()`, which the lines before 1.21.11 want as an empty list.

## This line

- Java 21, Mojang mappings with Parchment. `org.gradle.java.home` is pinned in `gradle.properties` to a JDK on this machine; if the daemon fails to start, check that path first.
- Datagen: `:neoforge:runData`. Output: `common/src/generated/resources`, never hand-edited.
- Game tests in `common/src/gametest`, run on both loaders: `:neoforge:runGameTest` and `:fabric:runGameTest`. Laid out with the test shim above; each loader's `GameTestRegistration` and `TestLoader` are in its own `src/test` (`architectury.md`, "Game tests on Fabric too"). From 1.21.5 Fabric API's own registration takes one annotated method per test, so Fabric's `GameTestRegistration` is the test mod's `main` entrypoint and registers into vanilla's test registries itself, the instances from Fabric's `DynamicRegistrySetupCallback`. NeoForge 21.10 and 21.11 post `RegisterGameTestsEvent` only while the registries load, before they freeze, so NeoForge's registers wherever it comes, the dedicated server included.
- 13 play scenarios in `scenarios/`, and 1 in `scenarios/server`.
- Entities and block entities save through 1.21.6's `ValueOutput` and read through `ValueInput`. A leaded glass design's colours go through `LeadedGlassConfig.putColors` and `getColors`, a list of dye names that also reads 1.1's array of numbers; `TestCompat.reloaded` saves and loads an entity the same way.
- Particles are 1.21.9's: the mod's own (`LeadSpeckParticle`) are `SingleQuadParticle`s given their sprite as they are made, and only a `SingleQuadParticle` takes a colour, so the purple smoke and fumes tint those. NeoForge's dev client needs no `-Dneoforge.warnings.onlyin.hide`, as 1.21.8's does: Architectury API 19 has no `@OnlyIn`, so NeoForge's warning screen about it doesn't come up.
- Sprites the mod makes as an atlas loads (`MurkyWater`, `LeadWaterParticles`) are 1.21.11's `SpriteSource.DiscardableLoader`s, and a made `SpriteContents` takes the source's texture section (blur, clamp, mipmaps) besides its animation, as vanilla's loader gives it. Contaminated water's textures are added to the blocks atlas (`atlases/blocks.json`); the lead trim palettes are the items atlas's from 1.21.11 (`atlases/items.json`).
- 1.21.11 content of its own: the Lead Spear and the Lead Nautilus Armor (with their recipes, tags, loot and nugget smelting), spears in the mob gear and chest swaps, parched in `can_wear_lead_armor`, and the `c:armors/horse` and `c:armors/nautilus` tags.
- JEI's plugin (`compat/jei`, JEI 27.6, pinned: newer builds use a Loom this line's refuses) shows the leaded glass recipes, info entries and, through `LeadedGlassSubtypes`, each design as its own stack; JEI runs in the dev client but not in the game tests or datagen. EMI has no build past 1.21.1.
- Fabric's config screen opens from Mod Menu (`TheLeadAgeModMenu`), compiled against Mod Menu 17.0.1, its newest for 1.21.11. Farmer's Delight Refabricated can't be on the classpath here (its 1.21.11 builds' classTweaker v2 breaks this Loom), so the Lead Knife compiles against a stub in `fabric/src/main/java/vectorwing`, kept out of every jar.
- Published with `publishMods` from `fabric/build.gradle` and `neoforge/build.gradle`, with the `-PpublishDryRun` flag. Uploads are tagged from `supported_minecraft_versions`.
- GitHub Actions: `release.yml`.
- Hand-authored access wideners and transformers: `common/src/main/resources/theleadage.accesswidener` and `neoforge/src/main/resources/META-INF/accesstransformer.cfg`. A first suspect when a port fails to load.
