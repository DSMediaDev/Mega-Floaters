# Changelog

All notable changes to Mega Floaters will be documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [0.5.0] - 2026-04-25

### Added
- **Mega islands** — a new structure-based generator places one giant
  island per ~32×32 chunk region. Six shapes, none of them circular:
  - **Plateau** — three concentric tiers eroded down toward the rim.
  - **Crater** — disc with a central basin flooded with Levitite Blend
    (Create Aeronautics). This is now the **only** natural source of
    Levitite Blend — underground pools and sky formations have been
    removed from DS: Skybound.
  - **Archipelago** — three to seven sub-mesas at staggered altitudes;
    overlapping subs blend into smooth saddles.
  - **Horseshoe** — C-shape ring with one quadrant cut open.
  - **Ridge** — narrow island at a 3:1 aspect ratio, randomly rotated.
  - **Atoll** — outer ring with a shallow basin inside.
  Edges are perturbed by deterministic 2D noise (±4 blocks), top
  surfaces roll ±2 blocks, and no seams appear at chunk boundaries.
  Mega islands roll radius 60–100 and thickness 28–56 blocks.
- **Overworld-style layering on mega islands.** Cores carry the full
  stratigraphy: top → 3 sub-surface layers → stone core with ~18%
  granite/diorite/andesite speckles → deepslate transition (bottom 8
  layers of thick cores) → underside. Ores scatter at ~2× overworld
  density, each in its correct Y band. Deepslate ore variants inside
  the deepslate zone. Non-stone cores (terracotta, end stone) keep
  their palette unchanged.
- **Pearlescent levitite in mega-island cores** (Create Aeronautics).
  Sparse scatter of `pearlescent_levitite` embedded in stone — visual
  "this is why it floats" reward when strip-mining.
- **Hanging vines on grass-topped mega islands.** Rim columns in
  jungle/forest/plains/swamp biomes grow 2–6 blocks of vanilla vines
  hanging beneath the island. Desert, badlands, snow, and end stay bare.
- **Vegetation on mega islands.** Ground cover (short grass, ferns,
  flowers, dead bushes) scatters at palette density across all solid
  top-surface positions per chunk. Trees attempt up to `min(maxTrees,
  3)` placements per chunk from interior positions — a forest island
  fills in with oaks and birch while desert stays bare.
- **Ponds on mega islands.** Eligible biomes (grass, forest, taiga,
  mangrove, jungle) have an 85% chance of a circular pond carved into
  the top surface. Radius ≈ island radius ÷ 12 (~5–8 blocks for a
  typical island).
- **Ancient ruins on mega islands.** 10% of mega islands spawn a
  crumbling cobblestone ruin with a loot chest, on an interior
  top-surface position ~65% of the island radius from its centre.
- **Dragon nests on mega islands.** 15% of mega islands spawn a
  cobblestone-rimmed sand nest, in a different direction from any ruin.
  BDD eggs populate normally when Bluedude Dragons is loaded.
- **Mega islands in IslandRegistry.** The anchor-chunk piece writes one
  `IslandRecord` per island with a stable deterministic UUID (no
  rediscovery after a world reload). Archetype IDs:
  `megafloaters:mega_<shape>`.
- **`IslandDiscoveredEvent` fires at the rim for mega islands.** The
  discovery radius is now `max(32, island.radius())` per island.
  Approaching a mega island's edge triggers discovery immediately
  instead of requiring a walk to the centre.

### Fixed
- **Hanging vines now hang vertically and connect.** The previous pass
  set `VineBlock.UP=true`, rendering each block as a horizontal top-face
  sheet — chains looked like floating green discs. Vines now use a
  randomly-rolled cardinal face per chain so blocks read as vertical
  sheets forming a continuous curtain.
- **Vine density halved.** Previously every eligible rim column grew
  vines; now ~50% do.
- **Ores no longer poke out of mega-island sides.** Air-exposed
  positions (rim columns, vertical steps between PLATEAU tiers) receive
  the palette core block instead of ore. Stone variants and pearlescent
  levitite scatter are unaffected.
- **Crater pools now fill to the brim.** The fill ceiling was at the
  un-noised rim Y, leaving it 1–2 blocks below the actual rim wherever
  top-surface noise pushed the rim up. The ceiling is now padded by the
  noise amplitude.
- **Hostile-mob suppression on mega islands.** Every chunk in the
  mega-island footprint is flagged `no_hostiles` so natural monster
  spawns are cancelled across the full island, matching the behaviour
  of satellite islands.
- **Spire and cone islands no longer have flat bottoms.** Spires now
  taper from full body width to half-width over the bottom third.
  Cone islands lost the 1-block ring ledge at the disc-to-cone seam.
  Every island archetype now has a properly tapered underside.

### Changed
- **Satellite island rarity 4× lower than v0.3.0** (base_rarity 15 →
  60 overworld, 15 → 30 end). Mega islands are now the primary sky
  landmark; satellites are scattered accent pieces between them.
- **CRATER shape weight bumped** (1.5 → 2.2) so the levitite-bearing
  crater accounts for ~1 in 4 mega islands, now that it is the sole
  natural source of Levitite Blend.
- **Mesa-style islands get a tapered underside.** The bottom 50% of a
  mesa's vertical extent narrows from full radius at the midpoint to 50%
  radius at the base. Cluster sub-discs pick up the same taper.

## [0.4.2] - 2026-04-22

### Fixed
- **BDD crash now patched at the source instead of blocking spawns.**
  v0.4.1 worked around BDD v1.3.0-alpha's server-crashing
  `fireProjectile` by cancelling every natural BDD spawn, which
  removed wild dragons from the world entirely. v0.4.2 replaces that
  with a Mixin injection into `BddAbilityDragon.fireProjectile` that
  cancels the method body on dedicated servers — the client-only
  keybinds class the method relies on is never loaded, so the tick
  thread survives. Wild dragons now spawn normally and can bite and
  melee. They can't use their ranged projectile attack on servers
  until BDD ships an upstream fix, but they exist, persist, and
  interact with the world again.

## [0.4.1] - 2026-04-22

### Fixed
- **Bluedude Dragons server-crash workaround (superseded by 0.4.2).**
  Natural, chunk-gen, and patrol spawns of any `bdd:*` entity were
  cancelled via `FinalizeSpawnEvent` whenever BDD was loaded. This
  dodged a server crash in BDD v1.3.0-alpha where
  `BddAbilityDragon.fireProjectile` loads a client-only keybinds
  class on the dedicated server and kills the tick thread.

## [0.4.0] - 2026-04-22

### Added
- **Mesa-style islands get a tapered underside.** The bottom 50% of
  the mesa's vertical extent narrows from full radius at the midpoint
  down to 50% radius at the very bottom. Small mesa-shape islands no
  longer look like floating dinner plates — they actually float.
  Cluster sub-discs pick up the same taper.

### Changed
- **Aeronautics levitite pools are now gated on massive islands only**
  (radius ≥ 12 post-multiplier). Smaller floaters skip the pool
  entirely since it tended to take up their whole top surface.
  Underside pearlescent scattering still runs on all sizes.
- **Minimum island radius bumped from 6 to 10.** Smaller floaters
  (radius 6–9) no longer generate, biasing the mix toward mid-to-large
  islands.

## [0.3.1] - 2026-04-22

### Fixed
- **Critical**: `DiscoveryTracker.onPlayerTick` crashed every player
  tick post-login after v0.3.0 with `UnsupportedOperationException` —
  the attachment codec returns immutable lists, and I was calling
  `.add()` on them directly. Fixed by copying into `ArrayList` and
  writing back via `setData`.

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
