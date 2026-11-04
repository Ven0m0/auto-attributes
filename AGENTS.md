# AGENTS.md

Client-only Fabric mod (id `autoattributes`) for Minecraft 26.3: one-key attribute swap-attack and auto spear lunge. See README.md for user-facing behavior.

## Commands

```sh
./gradlew build                  # compile, test, spotlessCheck, PMD
./gradlew spotlessApply build    # format first (google-java-format 2.0.0, 2-space indent)
./gradlew test                   # JUnit only
./gradlew pmdMain pmdClient pmdTest
./gradlew runClient              # dev client, Mod Menu is loaded in dev runs only
```

Run `spotlessApply` before committing; `spotlessCheck` is part of `build` and fails on unformatted code.

## Layout

- `src/main/java/.../`: Minecraft-free logic so it is unit testable. Keep new pure logic here.
  - `AttackTiming`: tick clock, show-then-restore window, lunge rate limit.
  - `SlotResolver`: configured slot, then hotbar scan (first match, or best score via `resolveBest`).
  - `ModConfig`: immutable records, defaults, `sanitized()` clamping, 0/1-based UI slot helpers. `WeaponType` is a plain enum.
- `src/client/java/.../client/`: everything that touches Minecraft classes.
  - `AutoAttributesClient`: entrypoint, registers keybinds (unbound by default) and the tick hook.
  - `SwapController`: thin adapter that feeds game state to `AttackTiming`/`SlotResolver` each tick (runs at `START_CLIENT_TICK`).
  - `ConfigStore`: Gson read/write of `config/autoattributes.json`; always goes through `sanitized()`.
  - `WeaponMatcher`: maps `WeaponType` to item tags.
  - `ModMenuIntegration`: Cloth Config screen; take slot ranges and conversions from `ModConfig`.
  - `mixin/`: accessors for `Minecraft` and `MultiPlayerGameMode` internals.
- Split source sets (`splitEnvironmentSourceSets()`): `main` cannot reference `client` classes.
- Tests: `src/test/java`, JUnit 5 (`SlotResolverTest` is the pattern; `AttackTimingTest`, `ModConfigTest`).

## Gotchas

- Slots are 0-based in code and JSON, 1-9 in the UI. `carrierSlot == -1` means "currently held slot".
- Swap and lunge share `showAttack`: select the target slot, flush the slot packet and attack in the same tick (the enchantment trick needs the attack to happen with the target held), then restore the original slot `showTicks` ticks later (skipped if the player scrolled away) so the item is visible to anticheats. Never move the attack itself to a later tick.
- Config is hand-editable, so any new field needs a default and clamping in `ModConfig.sanitized()` (and a range test in `ModConfigTest`).
- Java 25 target; Minecraft 26.3 uses Mojang mappings names (`Identifier`, `KeyMapping`), not Yarn. Check names against the current game version before assuming an API exists.
- `fabric_api_version` is pinned to the 26.3 build and Renovate updates for it are disabled; do not bump it to a 26.4 build.
- Dependency versions live in `gradle.properties`, not `build.gradle`.

## Conventions

- Immutable data (records, `List.copyOf`); new objects instead of mutation.
- Comments explain why, not what.
- No emojis or em dashes in code, comments, docs or commits.
- Update `CHANGELOG.md` for user-visible changes. `mod_version` is overridden in CI with `-Pmod_version=x.y.z`; release workflow validates jar names against the tag.
- PMD rules: `config/pmd/ruleset.xml` (errorprone + bestpractices, noisy rules excluded). Fix findings rather than suppressing.
