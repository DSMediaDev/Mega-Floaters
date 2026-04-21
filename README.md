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
- **KubeJS** — server-scripts API for registering archetypes and
  overriding palettes.

## License

LGPL-3.0-or-later. See [LICENSE](LICENSE).

## Credits

Authored by DentalStone.
