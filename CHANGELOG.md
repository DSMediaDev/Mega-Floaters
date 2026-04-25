# Changelog

All notable changes to Mega Floaters will be documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Fixed
- **Hanging vines now hang vertically and connect.** The previous
  pass placed each vine with the `up` face attached, which renders
  as a horizontal sheet on top of every block — chains looked like
  detached green discs. Vines now use a randomly-rolled cardinal
  face (north/south/east/west) per chain, so each block reads as
  a vertical sheet and stacked blocks form a continuous curtain.
- **Vine density halved.** Previously every grass-topped rim column
  grew vines; now ~50% do. Less curtain, more accent.
- **Ores no longer poke out of mega-island sides.** Air-exposed
  positions (rim columns, vertical steps between PLATEAU tiers) get
  the palette core block instead of ore. Stone variants and the
  pearlescent levitite scatter still appear normally — only the
  free-coal-from-the-sky-island problem is gone.
- **Crater pools now fill to the brim.** The pool ceiling was at
  the un-noised rim Y, which left it sitting 1–2 blocks below the
  actual rim wherever the rim's top noise pushed up. Pool ceiling
  now pads up by the rim noise amplitude so the levitite reaches
  every part of the rim.

### Added
- **Overworld-style layering on mega islands.** The exterior is no
  longer a single block of stone. Cores now carry the familiar
  stratigraphy: top (grass/sand/etc) → 3 layers of sub-surface
  (dirt/sandstone) → stone core with ~18% granite/diorite/andesite
  speckles → deepslate transition for the bottom eight layers of
  thick cores → underside. Ores scatter through the core at roughly
  2× overworld density (coal, iron, copper, redstone, gold, lapis,
  diamond — each with the right Y-band so diamonds don't appear 4
  blocks from the grass). Deepslate variants are used inside the
  deepslate zone. Non-stone cores (badlands terracotta, end stone)
  keep their palette core unchanged for now.
- **Pearlescent levitite sprinkle.** Where Create Aeronautics is
  installed, mega islands carry a scattering of pearlescent_levitite
  blocks embedded in their stone cores — a cheap visual "this is why
  it floats" reward for strip-mining.
- **Hanging vines on grass-topped mega islands.** Rim columns in
  jungle/forest/plains/swamp palettes grow 2–6 blocks of vanilla
  vines hanging beneath the island. Desert, badlands, snow, and end
  palettes stay bare.
- **Crater mega islands fill with Levitite Blend.** The newly-carved
  basin on CRATER-shape mega islands floods with aeronautics:levitite_blend
  from the basin surface up to rim level. This is now the **only**
  natural source of Levitite Blend in the pack — the old
  `megapack:levitite_pool` (underground) and `levitite_sky_formation`
  (on Rob's Floating Islands) features will be removed from the
  DS: Skybound modpack in the next pack release. Expect to fly, not dig.

### Changed
- **CRATER shape weight bumped** (1.5 → 2.2) so the levitite-bearing
  crater variant accounts for roughly one in four mega islands instead
  of one in six, now that it's the sole natural source of Levitite
  Blend.
- **Smaller satellite islands spawn less often.** The small-island
  placement rarity moved from 1-in-15 chunks to 1-in-30, cutting
  overall satellite density roughly in half. Mega islands are now
  the intended primary landmark; satellites are incidental cover
  around them. A proper anchor-and-satellite clustering pass (the
  "one MASSIVE island + smaller ones close by" layout) still to come.
- **Mega islands stopped looking like WorldEdit primitives.** Outer
  rims, top surfaces, and (where applicable) inner rings, crater
  lips, and horseshoe wedge cuts are all perturbed by deterministic
  2D value noise. Edges roll ±4 blocks; the top surface roll is ±2
  blocks. Adjacent chunk pieces sample the same noise field so no
  seams show up at chunk boundaries. Archipelago shapes also got a
  bell-curve blend between overlapping sub-mesas — joins now read
  as smooth saddles instead of step-cut spheres.

### Added
- **Mega islands (Phase A.2 — geometry only, no surface features yet).**
  A new structure-based generator places one rare giant island per
  ~32×32 chunk region. Mega islands roll a radius of 60–100 blocks
  and a thickness of 28–56 blocks — roughly 5–10× the size of the
  existing satellite islands. Six brand-new shapes ship with this
  release, none of them perfectly round:
  - **Plateau** — three concentric tiers stepping down toward the
    rim, like a layered Avatar mountain.
  - **Crater** — disc with a central depression carved into the top
    (basin gets flooded once Phase A.3 lands its sub-feature pass).
  - **Archipelago** — three to seven large sub-mesas at staggered
    altitudes inside a single footprint, reading as a chain of
    overlapping terraces.
  - **Horseshoe** — C-shape ring with one quadrant cut open; the
    opening direction is random per island.
  - **Ridge** — long, narrow island with a 3:1 length-to-width
    ratio, randomly rotated about its center. Stretches across far
    more chunks than its width suggests.
  - **Atoll** — outer ring with a shallow basin in the middle; A.3
    will fill the basin with water.
  Vegetation, ponds, ores, and ruins land in the next update — for
  now mega islands are bare terrain with biome-matched surface
  blocks. Hostile-mob suppression and registry integration also
  arrive in subsequent updates, so for v0.5.0-alpha purposes treat
  them as a "go look at the geometry" milestone, not a finished
  feature.

### Changed
- **Spire and cone islands no longer have flat bottoms.** Spires now
  taper from full body width down to half-width over the bottom third
  of their height, matching the taper convention already used by mesas
  and clusters. Cone islands lost the 1-block ring ledge that used to
  sit at the disc-to-cone seam — the underside now reads as one
  continuous taper from disc bottom to point. Every island archetype
  has a properly tapered underside now.

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
