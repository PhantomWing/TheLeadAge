# 1.2.0

### Additions
- Added toxic effects to the **Block of Raw Lead**:
  - Standing on it gives Lead Sickness.
  - Mining it releases lead fumes (except with Silk Touch).
  - Placed in water, it contaminates the water around it. Swimming in contaminated water gives Lead Sickness.
  - A campfire placed on it gives off toxic smoke. Add a hay bale in between for a signal fire.
- Added **Lead Pressure Plate** (weighted, only pressed by mobs and players).
- Added large lead ore veins between Y 8 and 58 (like copper and iron veins).
- Added smelting and blasting recipes for lead tools and armor into Lead Nuggets.
- Added a recipe to dye 8 Leaded Glass Panes at once.
- Added leaded glass in every color from dye mods (Dye Depot, Delicate Dyes).
- Added names to all tags (shown in JEI and EMI).
- Dyed leaded glass panes now tint beacon beams.
- Zombies, husks, zombie villagers and vindicators can now spawn with lead weapons and tools.
- Lead tools and armor can now be found in chests.
- Added config options for lead water contamination range, lead ore veins and mobs spawning with lead gear.

### Changes
- Lead fumes are now purple and spread out wider.
- Dyed leaded glass is now tinted by its dye.
- Trial chamber vaults can now give Lead Ingots.
- Added husks to the `theleadage:can_wear_lead_armor` tag.
- Added Lead Ore to the `minecraft:snaps_goat_horn` tag.
- Added the Leaded Glass Door to the `minecraft:mob_interactable_doors` tag.
- Added the Lead Weight damage type to the `minecraft:damages_helmet` and `minecraft:bypasses_shield` tags.
- Added the Lead Pickaxe to the `minecraft:cluster_max_harvestables` tag.
- Added leaded glass to the `c:glass_blocks` and `c:dyed` tags.
- Added the Lead Chain to the `c:chains` tag.
- Added lead ore to the `c:ores_in_ground` tags.
- Recipes now use the `c:ingots/lead` and `c:nuggets/lead` tags.

### Fixes
- Fixed 4 lead ingots crafting 4 Lead Bricks instead of 1.
- Fixed armor trims not showing on lead armor in the inventory.
- Fixed dyed leaded glass names in several languages.
- Fixed a crash when breaking leaded glass in a color from a dye mod.


# 1.1.2

### Fixes
- Fixed leaded glass panes placed on the floor or ceiling showing their design mirrored and rotated.
- Fixed leaded glass door designs appearing mirrored depending on the hinge side and whether the door is open.


# 1.1.1

### Fixes
- Fixed a crash on NeoForge when a Lead Weight started to fall.
- Fixed a crash on NeoForge when wearing lead armor underwater.
- Fixed the EMI integration failing to load, which hid all leaded glass recipes and in-world interactions.


# 1.1.0

### Additions
- Lead tools and armor release toxic fumes when they break, dosing players standing nearby.
- Added **Lead Bulb** (variant of the Copper Bulb)

### Changes
- Lead blocks now shield against explosions (except charged creepers).
- Lead and raw lead blocks are softer to mine, matching the rest of the lead set.
- Leaded glass and its panes now visibly shatter when broken without a pickaxe.
- Lead Weight can now be placed by a dispenser.

### Fixes
- Fixed Toolsmiths and Weaponsmiths being able to offer the same lead ingot trade twice on Fabric


# 1.0.0
First release. A vanilla-friendly expansion that adds lead — a heavy, toxic new metal. Sister mod to The Silver Age.

### Additions
- Lead ore generates throughout the Overworld in iron-like veins, with richer deposits in swamps. Smelts into Lead Ingots.
- A full lead tools & armor set — a "glass cannon" with netherite-level power but the lowest durability of any tier. Lead armor also weighs the wearer down.
- Lead Horse Armor
- **Leaded Glass**, in clear and all 16 dye colors, plus glass panes in ten came patterns (grid, diamond, lattice, cross, bars and more).
  - Customize placed panes in-world: right-click a region with a dye to color it, shears to make it clear again, sneak-right-click to rotate the pattern.
  - Leaded glass doors and trapdoors carry their own glass design — dyeable and shearable directly, and re-glazable with a pane in hand.
- **Lead Weight** — a falling block that crushes what's under it, hangs from chains, and gets damaged on each hard landing.
- **Lead Sickness** — mining lead ore or lingering by a Lead Torch releases toxic fumes (Hunger → Weakness → Poison + Nausea).
- Creepers and pillagers keep their distance from lead torches & lanterns.
- A full decorative lead set: bricks, slabs, stairs, walls, chiseled lead, pillars, grates, bars, chains, doors, trapdoors, torches and lanterns.
- Solid lead blocks are too heavy for pistons to push, and block sculk vibrations. They're also impermeable to water and lava.
- Lead ingots appear in structure loot, and armorer / toolsmith / weaponsmith villagers buy them.
- The Fishing Rod is crafted with a lead nugget for weight; the Heavy Core is optionally craftable from lead + netherite.
- Advancements, and full localization in 17 languages (auto-translated — corrections welcome).
- Configuration options for ore generation, lead sickness, and more.

### Compatibility
- **Create** — crush lead ore into Crushed Raw Lead (and smelt/wash it back into ingots and nuggets), and press ingots into Lead Sheets.
- **Sable (Create: Aeronautics)** — every lead block is weighted; lead is heavy.
- **Farmer's Delight** — adds the Lead Knife.
- **JEI** and **EMI** — full recipe support, including the in-world pane dyeing, shearing and rotating.
