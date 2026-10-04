@../MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age

**This branch is `version/26.3` — the 26.3 line.** Confirm with
`git branch --show-current` before trusting it; the registry at
`MinecraftDeveloperPortal/data/mods.json` lists every line this mod has.

No Parchment: 26.x ships unobfuscated, so Mojang names are the runtime names. `minecraft_version` is the build target; `supported_minecraft_versions` is the set tagged on uploads (see `ext.manifestProps` in `build.gradle`).

`scripts/` holds `datagen.ps1` and `genpanes.js`.
