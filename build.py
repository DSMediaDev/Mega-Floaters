#!/usr/bin/env python3
"""
Local javac build for Mega Floaters.

Why this exists: the Claude Code bash sandbox blocks loopback TCP,
which permanently rules out gradle (its daemon talks over 127.0.0.1).
This script reproduces what gradle's `build` task does for us using
only javac + jar, with the compile classpath pulled from an
already-populated ~/.gradle/caches/ tree.

Not a replacement for gradle — the gradle build is canonical. This is
the fast development loop.

Usage:
  ./build.py                  build with version from gradle.properties
  ./build.py --version=0.1.1  override version
  ./build.py --clean          wipe build/ first
  ./build.py --verbose        dump classpath and javac command
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent
SRC_JAVA = REPO_ROOT / "src" / "main" / "java"
SRC_RES = REPO_ROOT / "src" / "main" / "resources"
BUILD_DIR = REPO_ROOT / "build"
CLASSES_DIR = BUILD_DIR / "classes"
RES_DIR = BUILD_DIR / "resources"
LIBS_DIR = BUILD_DIR / "libs"

GRADLE_CACHE = Path.home() / ".gradle" / "caches"


def read_gradle_properties() -> dict[str, str]:
    """Parse gradle.properties into a dict."""
    path = REPO_ROOT / "gradle.properties"
    props: dict[str, str] = {}
    if not path.exists():
        return props
    for line in path.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, _, val = line.partition("=")
        props[key.strip()] = val.strip()
    return props


def read_gradle_property(key: str) -> str | None:
    return read_gradle_properties().get(key)


def find_java() -> Path:
    """Locate a Java 21 `javac`."""
    candidates = [
        Path("/usr/lib/jvm/java-21-openjdk-amd64/bin"),
        Path.home() / ".local" / "jdk" / "jdk-21.0.10+7" / "bin",
    ]
    for c in candidates:
        javac = c / "javac"
        if javac.exists():
            return c
    # fall back to PATH
    which = shutil.which("javac")
    if which:
        return Path(which).parent
    sys.exit("ERROR: javac not found. Install OpenJDK 21.")


def find_jar(pattern: str, min_classes: int = 0) -> Path | None:
    """Find a cached jar whose name matches `pattern`."""
    for p in GRADLE_CACHE.rglob(pattern):
        if p.is_file() and p.suffix == ".jar":
            return p
    return None


def discover_classpath(verbose: bool) -> list[Path]:
    """Locate MC, NeoForge, and DFU jars in the gradle cache."""
    jars: list[Path] = []

    # Minecraft joined deobf jar — NeoGradle emits it under ng_execute.
    # Pick the largest output.jar we can find (the MC one is ~40MB).
    mc_candidates = sorted(
        GRADLE_CACHE.glob("ng_execute/*/output.jar"),
        key=lambda p: p.stat().st_size,
        reverse=True,
    )
    if not mc_candidates:
        sys.exit(
            "ERROR: no Minecraft deobf jar found in ~/.gradle/caches/ng_execute/.\n"
            "       Run `./gradlew build` once in an unsandboxed shell to populate."
        )
    jars.append(mc_candidates[0])

    neoforge = find_jar("neoforge-21.1.*-universal.jar")
    if not neoforge:
        sys.exit("ERROR: NeoForge universal jar not found in gradle cache.")
    jars.append(neoforge)

    dfu = find_jar("datafixerupper-*.jar")
    if not dfu:
        sys.exit("ERROR: DataFixerUpper jar not found in gradle cache.")
    jars.append(dfu)

    if verbose:
        print("Classpath:")
        for j in jars:
            print(f"  {j}")

    return jars


def collect_java_sources() -> list[Path]:
    if not SRC_JAVA.exists():
        return []
    return sorted(SRC_JAVA.rglob("*.java"))


def expand_gradle_placeholders(props: dict[str, str]) -> None:
    """Expand `${key}` placeholders in neoforge.mods.toml using `props`.

    Mirrors what gradle's `processResources { expand(...) }` does in
    build.gradle, so the two build paths produce identical output.
    """
    toml = RES_DIR / "META-INF" / "neoforge.mods.toml"
    if not toml.exists():
        return
    text = toml.read_text()

    def repl(m: re.Match[str]) -> str:
        key = m.group(1)
        if key not in props:
            raise SystemExit(
                f"ERROR: neoforge.mods.toml references ${{{key}}} but it's "
                f"not set in gradle.properties."
            )
        return props[key]

    text = re.sub(r"\$\{([A-Za-z_][A-Za-z0-9_]*)\}", repl, text)
    toml.write_text(text)


def run(cmd: list[str], verbose: bool) -> None:
    if verbose:
        print("$", " ".join(str(c) for c in cmd))
    subprocess.run(cmd, check=True)


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", help="Override mod version")
    ap.add_argument("--clean", action="store_true", help="Wipe build/ first")
    ap.add_argument("--verbose", action="store_true")
    args = ap.parse_args()

    props = read_gradle_properties()
    if args.version:
        props["mod_version"] = args.version
    version = props.get("mod_version", "0.0.0")
    mod_id = props.get("mod_id", "megafloaters")
    print(f"Building {mod_id} {version}")

    if args.clean and BUILD_DIR.exists():
        shutil.rmtree(BUILD_DIR)
    BUILD_DIR.mkdir(exist_ok=True)
    CLASSES_DIR.mkdir(exist_ok=True)
    LIBS_DIR.mkdir(exist_ok=True)

    java_bin = find_java()
    javac = java_bin / "javac"
    jar = java_bin / "jar"

    sources = collect_java_sources()
    if sources:
        classpath = discover_classpath(args.verbose)
        cp_str = ":".join(str(p) for p in classpath)
        run(
            [
                str(javac),
                "-d", str(CLASSES_DIR),
                "-cp", cp_str,
                "--release", "21",
                *[str(s) for s in sources],
            ],
            args.verbose,
        )
    else:
        print("No Java sources yet — building resources-only jar.")

    # Copy resources.
    if RES_DIR.exists():
        shutil.rmtree(RES_DIR)
    if SRC_RES.exists():
        shutil.copytree(SRC_RES, RES_DIR)
    else:
        RES_DIR.mkdir()

    expand_gradle_placeholders(props)

    out_jar = LIBS_DIR / f"{mod_id}-{version}.jar"
    if out_jar.exists():
        out_jar.unlink()

    # Build jar with classes + resources.
    jar_cmd = [str(jar), "cf", str(out_jar)]
    if CLASSES_DIR.exists() and any(CLASSES_DIR.rglob("*.class")):
        jar_cmd += ["-C", str(CLASSES_DIR), "."]
    if RES_DIR.exists() and any(RES_DIR.iterdir()):
        jar_cmd += ["-C", str(RES_DIR), "."]
    run(jar_cmd, args.verbose)

    print(f"OK  {out_jar.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()
