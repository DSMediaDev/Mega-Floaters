# Mega Floaters

A NeoForge 1.21.1 mod that procedurally generates floating islands in the
overworld. Five archetypes (Disc, Cone, Mesa, Cluster, Spire) with
continuous-random sizing roll on a triangular distribution; biome-aware
surface palettes cover grass, desert, badlands, snowy, jungle, taiga,
mangrove, and end. Each island may carry vegetation, ponds and waterfalls,
ore veins in its core, and — at small odds per biome — a crumbling ancient
ruin with a tiered loot chest or a dragon nest. Optional integrations with
Create Aeronautics, Bluedude Dragons, FTB Quests, and KubeJS light up
automatically when those mods are present.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.222+

## Installation

Drop the jar into your server or client's `mods/` folder. In a packwiz
pack:

```
packwiz mr add dentals-mega-floaters
```

## Commands

All under `/megafloaters`, permission level 2.

| Command                                | Purpose                                                        |
|----------------------------------------|----------------------------------------------------------------|
| `/megafloaters reload`                 | Reload runtime config                                          |
| `/megafloaters spawn <archetype> [size]` | Force-place an island at your position                       |
| `/megafloaters list [radius]`          | List islands within a chunk radius (default 16)                |
| `/megafloaters info`                   | Show the nearest (≤64 blocks) registered island                |
| `/megafloaters regen <chunks> confirm` | Re-run the feature at every known island in range; destructive |
| `/megafloaters preview <archetype> [size]` | Particle outline of the given archetype                    |

## Configuration

Per-island knobs live in the `configured_feature` JSON and are
overridable by a datapack:

```jsonc
// data/megafloaters/worldgen/configured_feature/floater.json
{
  "type": "megafloaters:floater",
  "config": {
    "min_radius": 6,
    "max_radius": 24,
    "min_thickness": 4,
    "max_thickness": 14,
    "edge_chance": 0.6,        // outermost-ring thin-out probability
    "place_tree": true,        // enable vegetation tree placement
    "ruin_chance": 0.02,       // per-island ancient-ruin roll
    "nest_chance": 0.05,       // per-island dragon-nest roll
    "ore_count_multiplier": 1.5  // x ground-equivalent ore density
  }
}
```

The placement chain (`data/megafloaters/worldgen/placed_feature/floater.json`)
handles rarity, altitude, mountain-peak avoidance, and archipelago
clustering — tune there if you want a different density, altitude band,
or zone mix.

## Integrations

All optional; detected at runtime.

- **Create Aeronautics** — levitite pools on top of islands, pearlescent
  levitite scattered on the underside.
- **Bluedude Dragons** — dragon nests populated with biome-rolled egg
  species; natural dragon spawns within 48 blocks of an island have
  their spawn forced through.
- **FTB Quests** — `IslandDiscoveredEvent` fires once per island per
  player the first time they come within 32 blocks. Hook it as a
  NeoForge-event task in your quest pack.
- **KubeJS** — the public API is a plain static Java facade; call it
  directly from `server_scripts`. See "Scripting" below.

## Scripting (KubeJS)

The public API (`gg.dsmedia.megafloaters.api.MegaFloatersAPI`) is
plain static Java, so KubeJS `server_scripts` can call it directly.
Defensive Rhino patterns apply — use `let` rather than `const`, call
method-like accessors with parentheses, and avoid ES6 object shorthand.

Listening for island placement:

```javascript
// server_scripts/floaters.js
NeoForge.onEvent('gg.dsmedia.megafloaters.api.IslandPlacedEvent', event => {
    let info = event.getIsland()
    let pos = info.center()
    console.info('island placed: ' + info.archetype() + ' r=' + info.radius() + ' @ ' + pos)
})
```

Listening for first-discovery:

```javascript
NeoForge.onEvent('gg.dsmedia.megafloaters.api.IslandDiscoveredEvent', event => {
    let player = event.getPlayer()
    let info = event.getIsland()
    player.tell('You discovered a ' + info.archetype() + ' island!')
})
```

Reading island state:

```javascript
let API = Java.loadClass('gg.dsmedia.megafloaters.api.MegaFloatersAPI')
// API.getIslandAt(serverLevel, blockPos) → Optional<IslandInfo>
// API.getIslandsNear(serverLevel, blockPos, radiusBlocks) → List<IslandInfo>
```

`IslandInfo` exposes `id()`, `archetype()`, `center()`, `radius()`,
`thickness()`, `biome()`, `hasRuin()`, `hasNest()`, `hasLevitite()`,
and `placedAtTick()`.

## Public Java API

Other mods can depend on `gg.dsmedia.megafloaters.api.*` against a
stable contract. Breaking changes bump the minor version pre-1.0 and
the major version post-1.0.

- `MegaFloatersAPI.getIslandAt(ServerLevel, BlockPos)`
- `MegaFloatersAPI.getIslandsNear(ServerLevel, BlockPos, int radiusBlocks)`
- `MegaFloatersAPI.islandPlacedEvent()` → the NeoForge event bus
- `IslandPlacedEvent`, `IslandDiscoveredEvent` — posted on that bus
- `IslandInfo` — read-only island view
- `ArchetypeBuilder` — forward-compat interface for external archetypes;
  wired into placement in a later release.

## Troubleshooting

- **Islands don't appear in an existing world.** Feature placement only
  runs on newly-generated chunks. Pregen a fresh area with `/chunky` or
  explore outward past your prior world border.
- **Hostile mobs still spawn on my island.** Player-triggered spawns
  (spawners, spawn eggs, commands) are intentionally allowed. Natural
  spawns are cancelled.
- **Ore density seems off.** Tune `ore_count_multiplier` in the
  configured_feature JSON via a datapack.

## Building

Two build paths exist, both producing the same jar.

### Local build (javac)

```
./build.py
```

Requires Java 21 and the NeoForge 1.21.1 gradle cache populated at
`~/.gradle/caches/`. If the cache is empty (fresh machine), run
`./gradlew build` once outside any sandboxed shell to populate it, then
`build.py` can work from the cached jars.

### Gradle (canonical for releases)

```
./gradlew build
```

Output lands in `build/libs/`.

## License

LGPL-3.0-or-later. See [LICENSE](LICENSE).

## Credits

Authored by DentalStone.

## Source

Repository: https://github.com/DSMediaDev/Mega-Floaters
