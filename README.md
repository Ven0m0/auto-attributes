# Auto Attributes

Client-side Fabric mod for Minecraft 26.3 that swaps to a second weapon and attacks in a single key press, so your held weapon's attack benefits from the other weapon's enchantments. It also provides automatic spear lunging.

## Features

- **Attribute swap attack**: you equip your primary weapon yourself; one key swaps to a configurable secondary weapon (default Mace, or a Sword for Axe -> Sword), hits, and swaps back.
- **Auto spear lunge**: hold a key to lunge repeatedly, with a configurable extra delay (default 0, the fastest vanilla allows). The spear stays selected briefly after each jab, like a manual swap.
- **Configurable hotbar slots**, with a hotbar scan fallback when the configured slot does not hold the right item.
- **Mod Menu config screen** (Cloth Config). Keybinds appear in the vanilla Controls screen.

## How it works

Pressing the swap key while looking at an entity selects the target weapon's slot, sends the slot change, attacks, and selects the original slot again, all within one client tick. The server applies the target item's enchantments to the attack.

The lunge key attacks with a spear and leaves it selected for `showTicks` ticks (default 1) before restoring your slot, so the server sees a normal-looking swap instead of an instant flicker. It skips when sprinting is impossible (low food, riding, gliding, swimming) or the attack cooldown is not ready. If no spear is in the configured slot, it picks the hotbar spear with the highest Lunge level. The visible tick costs roughly one tick of maximum lunge rate.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the config screen)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3 with Java 25.
2. Put these jars in your `mods` folder: Fabric API, Cloth Config, and `auto-attributes-<version>.jar`. Add Mod Menu if you want the in-game config screen.
3. Start the game and bind the keys (see Usage).

The mod is client-only. Do not install it on a server.

## Usage

All keybinds start unbound. Bind them under **Options -> Controls -> Key Binds -> Auto Attributes**: one swap key and one lunge key.

Settings live in the Mod Menu config screen and are stored in `config/autoattributes.json`. Slots are 0-based in the file (1-9 in the UI); a lunge `carrierSlot` of `-1` means "whatever slot is currently held".

## Configuration

The swap has: `enabled`, `target` weapon (sword, axe, spear or mace) and `targetSlot`. The lunge has: `enabled`, `spearSlot`, `carrierSlot`, `delayTicks` (0 to 40 extra ticks between lunges) and `showTicks` (1 to 10 ticks the spear stays selected).

Set `targetSlot` or `spearSlot` to `-1` (0 in the UI) to always auto-detect the weapon by scanning the hotbar; for the spear this picks the highest Lunge level. A configured slot that holds the wrong item also falls back to this scan.

Defaults (slots shown 1-9 as in the UI): swap to a Mace in slot 9. Lunge: spear in slot 2, carrier slot is the currently held slot, delay 0, showTicks 1.

The file is hand-editable. Missing or out-of-range values fall back to defaults or are clamped when the config loads, and the file is rewritten with the corrected values on the next save from the config screen. Delete the file to reset everything.

## Building

Requires JDK 25. Gradle itself is downloaded by the wrapper, so you do not need to install it.

### Linux and macOS

```sh
./gradlew build
```

### Windows

Use PowerShell or cmd from the repo root. The wrapper is `gradlew.bat`:

```powershell
.\gradlew.bat build
```

In Git Bash, `./gradlew build` also works. If the build says it cannot find Java, install JDK 25 (for example `winget install EclipseAdoptium.Temurin.25.JDK`) and make sure `java -version` reports 25. If Gradle picks up another JDK, set `JAVA_HOME` for the session:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25"
```

### Output

The finished mod is `build/libs/auto-attributes-<version>.jar`. Ignore the `-sources.jar` next to it. Set the version with `-Pmod_version=x.y.z` (default is in `gradle.properties`).

### Common tasks

Use `.\gradlew.bat` instead of `./gradlew` on Windows.

| Command | What it does |
|---------|--------------|
| `./gradlew build` | Compile, test, Spotless check, PMD |
| `./gradlew remapJar` | Just the jar, skipping tests and checks |
| `./gradlew spotlessApply build` | Format with google-java-format, then build |
| `./gradlew test` | JUnit tests only |
| `./gradlew runClient` | Launch a dev client (Mod Menu is loaded here) |

`spotlessCheck` is part of `build` and fails on unformatted code, so run `spotlessApply` first. Dependency updates are handled by Renovate.

## Contributing

Source layout: pure logic that does not touch Minecraft lives in `src/main` (`SlotResolver`, unit tested). Everything that uses Minecraft classes is in `src/client`. See [AGENTS.md](AGENTS.md) for conventions and gotchas, and update `CHANGELOG.md` for user-visible changes.

## License

MIT, see [LICENSE](LICENSE). Release notes are in [CHANGELOG.md](CHANGELOG.md).
