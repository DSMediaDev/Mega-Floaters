# Mega Floaters

A NeoForge 1.21.1 mod that procedurally generates floating islands in the
overworld and the end. Five archetypes (Disc, Cone, Mesa, Cluster, Spire),
biome-aware surface palettes, vegetation, ponds, waterfalls, and
optional integrations with Create Aeronautics, Bluedude Dragons, FTB
Quests, and KubeJS.

Status: **pre-release scaffold**. v0.1.0 in progress.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.222+

## Building

Two build paths exist, both producing the same jar:

### Local build (javac)

```
./build.py
```

Requires Java 21 and the NeoForge 1.21.1 gradle cache populated at
`~/.gradle/caches/`. If the cache is empty (fresh machine), run
`./gradlew build` once outside any sandboxed shell to populate it, then
`build.py` can work from the cached jars.

### CI build (gradle)

```
./gradlew build
```

Output lands in `build/libs/`.

## Integrations

All optional; detected at runtime. No errors if absent.

- **Create Aeronautics** — levitite pools on top of islands, pearlescent
  levitite scattered on the underside.
- **Bluedude Dragons** — dragon nests with biome-rolled egg species,
  spawn buff near islands.
- **FTB Quests** — custom observation events for quest integration.
- **KubeJS** — server-scripts can read island state and react to
  island placement via the public API (see below).

## Scripting (KubeJS)

The public API (`gg.dsmedia.megafloaters.api.MegaFloatersAPI`) is a
plain Java static facade, so KubeJS `server_scripts` can call it
directly. Defensive Rhino patterns apply — use `let` rather than
`const`, treat method-like accessors as methods (call them with
parentheses), and avoid ES6 object shorthand.

Listening for island placement (fires in newly-generated chunks):

```javascript
// server_scripts/floaters.js
NeoForge.onEvent('gg.dsmedia.megafloaters.api.IslandPlacedEvent', event => {
    let info = event.getIsland()
    let pos = info.center()
    console.info('island placed: ' + info.archetype() + ' r=' + info.radius() + ' @ ' + pos)
})
```

Reading island state at a position (e.g. for a custom advancement trigger):

```javascript
let API = Java.loadClass('gg.dsmedia.megafloaters.api.MegaFloatersAPI')

ServerEvents.commandRegistry(event => {
    // ... register a command that calls API.getIslandAt(level, pos) ...
})
```

`API.getIslandsNear(serverLevel, blockPos, radiusBlocks)` returns a
Java list of `IslandInfo`. Each entry exposes `id()`, `archetype()`,
`center()`, `radius()`, `thickness()`, `biome()`, `hasRuin()`,
`hasNest()`, `hasLevitite()`, and `placedAtTick()`.

## License

LGPL-3.0-or-later. See [LICENSE](LICENSE).

## Credits

Authored by DentalStone.

## Source

Repository: https://github.com/DSMediaDev/Mega-Floaters
