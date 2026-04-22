# Changelog

All notable changes to Mega Floaters will be documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.3.0] - 2026-04-22

### Removed
- **Disc archetype**. Live-play feedback was that flat-pancake islands
  looked too uniform once several spawned near each other. The four
  remaining archetypes (mesa, cone, cluster, spire) cover the visual
  range without it. External mods wanting a disc can still register
  one via `MegaFloatersAPI.registerArchetype`.

### Changed
- **Island rarity 3x lower**: `base_rarity` default in
  `ArchipelagoPlacement` bumped from 5 to 15, matching the updated
  defaults in both `placed_feature/floater.json` and
  `placed_feature/floater_end.json`. Prior density produced full
  skies of floaters; new density leaves meaningful gaps between
  archipelago zones.
- `BiomeArchetypeWeights` is now a 4-element table per biome (the
  DISC column was folded into mesa + cluster).

## [0.2.0] - 2026-04-22

### Fixed
- End-tier ruin loot table now parses on 1.21.1. The mod-loaded-gated
  pack drops were using a condition syntax that isn't applied at
  loot-entry decode depth in NeoForge 1.21.1; they've been stripped
  for now and will be re-added via `LootModifyEvent`.
- Worldgen no longer spams the log with "Detected setBlock in a far
  chunk" errors. Island radius is capped at 14 blocks post-multiplier,
  tree placements are filtered to interior-only positions so canopies
  don't spill across chunk boundaries, and the Aeronautics underside
  embedding scan is tightened to `radius` exactly. Max practical
  island radius in this release is therefore 14; larger islands need
  structure-based generation which is slated for a later version.

### Added
- End-dimension support. The end palette, end biome weights, and
  end archetype table have been in the mod since v0.1.0 but the end
  biome modifier was missing — so floaters never actually spawned in
  The End. Now they do, with a separate placed feature that runs at
  y=60-140 (trapezoid plateau 16) and reuses the same configured
  feature as the overworld.
- External archetype dispatch. `MegaFloatersAPI.registerArchetype(id,
  builder)` and `getArchetype(id)` are now functional — registered
  archetypes can be spawned via `/megafloaters spawn <namespace:path>`
  or driven directly through the new
  `FloaterFeature.generateWithBuilder(...)` helper. Externals don't
  yet participate in biome-weighted natural placement, but they share
  the same post-build pipeline (vegetation, water, ores, structures,
  integrations, chunk flag, registry record) as built-in archetypes.
- Datapack-driven palette overrides. Drop a JSON file at
  `data/<ns>/megafloaters/palettes/<biome>.json` to replace the
  built-in palette for that biome, or at
  `data/<ns>/megafloaters/palettes/<biome>/<archetype>.json` for
  biome + archetype specific overrides. Resolution priority: biome +
  archetype → biome → built-in default. Hot-reloaded with `/reload`.
- Three more observation events complete the FTB Quests integration:
  - `RuinOpenedEvent` — posted on `NeoForge.EVENT_BUS` when a player
    right-clicks an unopened ruin chest. Fires exactly once per chest
    (opening consumes the loot-table reference), and carries the tier
    that was on the chest (iron / diamond / end).
  - `LevititeHarvestedEvent` — posted when a player right-clicks a
    levitite_blend block with an empty bucket near a known floater
    island. Requires Create Aeronautics to be installed.
  - `NestEncounteredEvent` — fires alongside `IslandDiscoveredEvent`
    the first time a player approaches a floater with a dragon nest,
    tracked separately on the player so nest-specific quests can fire
    without racing the discovery quest.

## [0.1.0] - 2026-04-22

First release.

### Added

#### World generation
- Floating islands in the overworld at y=150-290 with a trapezoid
  altitude distribution and organic rim thinning.
- Five island archetypes — disc, cone, mesa, cluster, spire — with
  continuous-random sizing from a triangular distribution.
- Biome-weighted archetype selection: taiga favours spires, jungle
  favours cones, ocean favours discs and clusters, badlands favours
  mesas, and so on.
- Biome-matched surface palettes: sand + sandstone in deserts, red
  sand over terracotta in badlands, snow over stone in snowy biomes,
  coarse dirt in taiga, mud over dirt in mangrove, end stone in the
  end, and the classic grass + dirt + stone everywhere else.
- Biome-appropriate surface content: per-palette tree list, ground
  cover, and pond/waterfall chances. Plains keep oaks and dandelions;
  forests mix oak and birch; savannas roll acacia; jungles dense
  with jungle trees and ferns; taiga spruce over coarse dirt;
  mangrove gets mangrove trees and frequent ponds; dark forest
  prefers dark oak; snowy biomes grow spruce; deserts and badlands
  scatter dead bushes.
- Ponds carve into top surfaces and can spawn a visible waterfall
  where they touch the rim.
- Ores scattered through each island's core using vanilla veins,
  weighted toward coal and iron with rare gold, redstone, lapis, and
  small diamond. Density scales with island radius and carries a
  1.5× multiplier over ground-equivalent distribution.
- Archipelago clustering: each 32×32-chunk region is classified as
  archipelago (3× density, 40% of regions), void (0% density, 20%),
  or normal (unchanged, 40%) — sky-dense and sky-sparse zones are
  visible at map scale.
- Mountain peak avoidance: placements reject rolls that fall within
  20 blocks of the ground heightmap below them.

#### Structures and loot
- Ancient ruins on 2% of islands: 5×5 mossy-cobblestone floor with
  partial crumbling walls, a random doorway, and a single loot chest.
- Three-tier loot rolled per chest: iron (60%), diamond (30%), end
  (10%).
- End-tier chests include mod-loaded-gated chances for Create
  Aeronautics levitite buckets, Create precision mechanisms, and
  Bluedude Dragons eggs when those mods are installed.
- Dragon nests on 5% of islands: cobblestone rim around a sand bed.

#### Mobs
- Hostile mobs no longer spawn on floater islands. Passive mobs,
  spawners, spawn eggs, and commands all still work — only natural
  monster spawns are cancelled.

#### Commands (`/megafloaters`, op level 2)
- `reload` — reload runtime configuration.
- `spawn <archetype> [size]` — force-place at the caller's position.
- `list [radius]` — list islands in a chunk radius.
- `info` — nearest-registered-island readout.
- `regen <chunks> confirm` — destructive re-run on registered islands.
- `preview <archetype> [size]` — particle outline at the caller.

#### Integrations
- **Create Aeronautics**: levitite blend pools on top (diameter
  scales with radius) plus pearlescent levitite on the underside.
- **Bluedude Dragons**: 1–3 biome-appropriate eggs in dragon nests;
  natural dragon spawns within 48 blocks of any registered island
  are forced through.
- **FTB Quests**: `IslandDiscoveredEvent` fires once per island per
  player the first time they come within 32 blocks. Hook it as a
  NeoForge-event task in your quest pack.
- **KubeJS**: the public API is plain static Java; call it directly
  from `server_scripts`. README includes examples.

#### API (`gg.dsmedia.megafloaters.api`, semver-tracked)
- `MegaFloatersAPI.getIslandAt`, `getIslandsNear`, `islandPlacedEvent`.
- Events: `IslandPlacedEvent`, `IslandDiscoveredEvent`.
- `IslandInfo` view interface.
- `ArchetypeBuilder` forward-compat interface.

#### Persistence
- Per-level `IslandRegistry` SavedData records every placed island
  (archetype, radius, thickness, biome, subfeature flags, center,
  game tick) and survives save/load.

### Deferred to a later release

- Drifting islands via Sable. The plan flagged this as a stretch
  spike with a clean fallback — static islands are shipping.
- Levitite-harvested, ruin-chest-opened, and nest-encountered
  observation events (the remaining three quest hooks beyond
  first-discovery).
- Datapack-driven palette overrides (JSON under `data/megafloaters/
  palettes/`). Built-in palettes cover vanilla biomes for now.
- External archetype registration via `MegaFloatersAPI.registerArchetype`.
  The interface ships but is a no-op in v0.1.
