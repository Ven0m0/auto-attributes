# Auto Attributes

Client-side Fabric mod for Minecraft 26.3 that swaps weapons and attacks in a single key press, so one weapon's attack benefits from another weapon's enchantments. It also provides automatic spear lunging.

## Features

- **Swap-attack combos**: three configurable combos, each on its own keybind. Defaults:
  - Sword -> Mace -> hit
  - Spear -> Mace -> hit
  - Axe -> Sword -> hit
- **Auto spear lunge**: hold a key to lunge repeatedly, with a configurable extra delay (default 0, the fastest vanilla allows).
- **Configurable hotbar slots** per weapon role, with a hotbar scan fallback when the configured slot does not hold the right item.
- **Mod Menu config screen** (Cloth Config). Keybinds appear in the vanilla Controls screen.

## How it works

Pressing a combo key:

1. If you are not holding the source weapon, the press equips it (switching resets charge, so no attack happens yet).
2. If you are holding it and looking at an entity, the mod selects the target weapon's slot, sends the slot change, attacks, and selects the original slot again, all within one client tick. The server applies the target item's enchantments to the attack.

The lunge key does the same with a spear, skipping when sprinting is impossible (low food, riding, gliding, swimming) or the attack cooldown is not ready. If no spear is in the configured slot, it picks the hotbar spear with the highest Lunge level.

## Requirements

- Minecraft 26.3
- Fabric Loader 0.20.1 or newer
- Java 25 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the config screen)

## Usage

All keybinds start unbound. Bind them under **Options -> Controls -> Key Binds -> Auto Attributes**: three combo keys and one lunge key.

Settings live in the Mod Menu config screen and are stored in `config/autoattributes.json`. Slots are 0-based in the file (1-9 in the UI); a lunge `carrierSlot` of `-1` means "whatever slot is currently held".

## Building

```sh
./gradlew build
./gradlew spotlessApply build   # format with google-java-format, then build
```

The build runs Spotless (google-java-format) and PMD. Dependency updates are handled by Renovate.

## License

MIT, see [LICENSE](LICENSE). Release notes are in [CHANGELOG.md](CHANGELOG.md).
