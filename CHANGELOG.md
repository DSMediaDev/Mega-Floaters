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
