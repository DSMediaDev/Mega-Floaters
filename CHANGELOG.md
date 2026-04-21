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
  end, and the classic grass + dirt + stone everywhere else. Trees
  are suppressed on non-grass palettes until the full vegetation
  system ships.
