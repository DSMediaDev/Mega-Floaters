# Changelog

All notable changes to Mega Floaters will be documented here.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Initial project scaffold (pre-release).
- Floating islands in the overworld at y=150-290 with a bell-curve
  altitude distribution, organic rim, and one oak tree on suitable
  archetypes.
- Five island archetypes: disc, cone, mesa, cluster, spire.
  Archetype is chosen per spawn from a biome-weighted table — taiga
  favours spires, jungle favours cones, ocean favours discs and
  clusters, badlands favours mesas, and so on. Island radius and
  thickness roll from a triangular distribution; spires apply
  additional tall-and-narrow multipliers.
- Biome-matched surface palettes: sand + sandstone in deserts, red
  sand over terracotta in badlands, snow over stone in snowy biomes,
  coarse dirt in taiga, mud over dirt in mangrove, end stone in the
  end, and the classic grass + dirt + stone everywhere else.
- Biome-appropriate surface content: each palette ships its own tree
  list, ground-cover blocks, and pond/waterfall chances. Plains keep
  oaks and dandelions, forests mix oak and birch, savannas roll
  acacia or oak, jungles fill with dense jungle trees and ferns,
  taiga grows spruce over coarse dirt, mangrove gets mangrove trees
  over mud with frequent ponds, dark forest prefers dark oak, snowy
  biomes get spruce over snow with no ground cover, and deserts and
  badlands scatter dead bushes. Ponds carve into the top surface and
  may spawn a visible waterfall where they touch the rim.
- Ores are scattered through each island's stone core using vanilla
  ore veins, weighted toward coal and iron with rare gold, redstone,
  lapis, and small diamond veins. Vein density scales with island
  radius squared and has a 1.5× multiplier over ground-equivalent
  distribution — stripping a floater should feel rewarding.
- Ancient ruins sit on 2% of islands: a 5×5 mossy-cobblestone floor
  with partial crumbling walls, a random doorway, and a single loot
  chest rolled from three tiers — iron (60%), diamond (30%), or end
  (10%). End-tier chests have a chance to drop Create Aeronautics
  levitite buckets, Create precision mechanisms, or Bluedude Dragons
  eggs when the respective mod is installed.
- Dragon-nest placeholders sit on 5% of islands: a cobblestone rim
  around a sand bed. Eggs are added by the Bluedude Dragons
  integration in a later release.
- Create Aeronautics integration: when the mod is installed, every
  floater gets a pool of levitite blend on its top surface (diameter
  scales with island radius) and a sparse scattering of pearlescent
  levitite on the exposed underside — the in-world reason the island
  floats. Skipped silently on packs without Aeronautics.
- Bluedude Dragons integration: dragon nests are populated with 1–3
  biome-appropriate eggs when BDD is installed. Nightfury eggs in
  snowy biomes, deadly nadder in plains/savanna, gronckle in forests,
  hideous zippleback in swamps and mangroves, monstrous nightmare in
  deserts and badlands, speed stinger in taiga and old-growth forests,
  and terrible terror everywhere else. The dragon spawn buff lands in
  a later release once the island registry is in place.
- Hostile mobs no longer spawn on floater islands. Passive and neutral
  mobs still spawn normally, and player-placed spawners, spawn eggs,
  commands, and every other non-natural source pass through
  unaffected — so mob farms and triggered spawns keep working.
- `/megafloaters` commands for operators (permission level 2):
  - `spawn <archetype> [size]` — force-place an island at your feet.
  - `list [radius]` — list every floater in a chunk radius (archetype,
    radius, center), truncated to 10 entries per invocation.
  - `info` — nearest-within-64-blocks island readout with archetype,
    size, biome, and subfeature flags.
  - `regen <chunks> confirm` — re-run the feature on every registered
    floater within range; destructive enough to require the explicit
    `confirm` subnode.
  - `preview <archetype> [size]` — one-shot particle outline of what
    an island of that archetype would look like at your position.
  - Existing `reload` keeps its green confirmation message.
- Persistent island registry: every generated floater is recorded in
  a per-level SavedData file (archetype, radius, thickness, biome,
  subfeature flags, center, game tick). Commands now query the
  registry directly, and the store survives save/load.
- Dragon spawn buff now live: when Bluedude Dragons is installed, a
  dragon attempting to spawn naturally within 48 blocks of any
  registered floater island has its spawn forced through.
- Public API (`gg.dsmedia.megafloaters.api`) — pre-1.0 stable surface
  other mods and scripts can target. `MegaFloatersAPI` exposes
  `getIslandAt`, `getIslandsNear`, and access to the event bus where
  `IslandPlacedEvent` is posted after each island finishes generating.
  `ArchetypeBuilder` ships as a forward-compat interface; external
  archetype registration is a no-op in v0.1 and will light up in a
  future release.
- KubeJS integration: `server_scripts` can subscribe to
  `IslandPlacedEvent` via `NeoForge.onEvent(...)` and call
  `MegaFloatersAPI` static methods directly. README includes example
  snippets with defensive Rhino patterns.
- First-discovery event: `IslandDiscoveredEvent` fires on
  `NeoForge.EVENT_BUS` the first time a player comes within 32 blocks
  of an island they've never been near before. Discovery lists are
  persisted on the player and survive death/save/load, so quest tasks
  that listen for this event fire exactly once per island per player.
  FTB Quests and any mod with a NeoForge-event task type can use this
  directly; bundled quests are not part of the mod.
- Archipelago clustering: islands no longer spread uniformly across
  the overworld. Each 32×32-chunk region is classified once as
  archipelago (3× density, 40% of regions), void (no islands, 20% of
  regions), or normal (unchanged, 40%). Visible at map scale; players
  get meaningful sky-dense zones to build on.
- Mountain peak avoidance: island placements now check the ground
  heightmap below them and reject any roll that falls within 20
  blocks of a tall peak. Floaters stay visually separate from the
  mountains they drift past.
