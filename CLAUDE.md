@../MinecraftDeveloperPortal/.claude/profiles/architectury.md

# The Lead Age

**This working copy is on `version/26.2` — the 26.2 line.** Confirm with
`git branch --show-current` before trusting it; the registry at
`MinecraftDeveloperPortal/data/mods.json` lists every line this mod has.

**The working tree holds an uncommitted 26.2 migration.** The committed content of this branch is still 26.1.2. Do not stash, reset or switch branches without asking.

No Parchment: 26.x ships unobfuscated, so Mojang names are the runtime names. `minecraft_version` is the build target; `supported_minecraft_versions` is the set tagged on uploads (see `ext.manifestProps` in `build.gradle`).

`scripts/` holds `datagen.ps1` and `genpanes.js`.
