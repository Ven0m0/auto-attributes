# Changelog

## Unreleased

- Replaced the three combos with one attribute-swap key: hold your primary weapon yourself, press the key to swap to a configurable secondary weapon (default Mace), hit, and swap back in the same tick.
- Auto lunge now leaves the spear selected for a configurable number of ticks (default 1) after the jab instead of swapping in and out within one tick, so the swap is visible to anticheats like a manual one.
- The attribute swap now also keeps the target weapon selected for a configurable number of ticks (`swap.showTicks`, default 1) after the hit. The attack still happens in the same tick as the swap.
- Swap target slot and spear slot accept 0 in the UI (-1 in the file) to always auto-detect the weapon on the hotbar. A wrong configured slot already falls back to a scan.
- Old `combos` entries in `config/autoattributes.json` are ignored; the swap falls back to defaults.

## 1.0.0

- Three configurable swap-attack combos (defaults: Sword -> Mace, Spear -> Mace, Axe -> Sword), each on its own keybind.
- Auto spear lunge on a hold-to-repeat keybind with a configurable extra delay.
- Configurable hotbar slot per weapon role, with a hotbar scan fallback.
- Mod Menu config screen (Cloth Config); keybinds appear in the vanilla Controls screen.
