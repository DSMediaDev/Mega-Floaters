# Contributing

## Development setup

### JDK

OpenJDK 21 is required. On Ubuntu/WSL:

```
sudo apt install openjdk-21-jdk
```

### First-time gradle cache populate

NeoGradle downloads Minecraft + NeoForge and produces deobfuscated
classpath jars into `~/.gradle/caches/`. Run once in an unrestricted
shell:

```
./gradlew build
```

Subsequent local builds can use `./build.py` (faster, no gradle daemon).

## Build paths

Two build paths exist. They must produce byte-compatible jars modulo
timestamps.

| Path | Command | When |
|---|---|---|
| Local javac | `./build.py` | Day-to-day development |
| Gradle | `./gradlew build` | Release, CI, verification |

After CI completes for a tag, download the Actions artifact and diff
the class files against a fresh local javac output. Class file hashes
should match.

## Commit conventions

- Commit messages focus on the *why*, not the *what*.
- Player-facing release notes live in `CHANGELOG.md`, not commit bodies.
- Co-author trailer on AI-assisted commits:
  `Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>`
- Never commit API tokens. `MODRINTH_TOKEN` lives in GitHub Actions
  secrets or `~/.config/`, never in the repo.

## Release workflow

1. Update `CHANGELOG.md` — move `[Unreleased]` entries into a new
   version section.
2. Bump `mod_version` in `gradle.properties`.
3. Tag: `git tag -a v0.1.0 -m "..."`, `git push --tags`.
4. CI builds the jar and attaches it to a GitHub release.
5. CI publishes to Modrinth via `MODRINTH_TOKEN` secret.
