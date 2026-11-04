package io.github.ven0m0.autoattributes;

/**
 * Immutable config. Slots are 0-based here and in the file (1-9 in the UI, see {@link #toUiSlot}).
 * A {@code carrierSlot} of -1 means "whatever slot is currently held". A {@code targetSlot} or
 * {@code spearSlot} of -1 means "auto-detect by scanning the hotbar". File IO lives in the client
 * {@code ConfigStore}; everything here is Minecraft-free so the ranges can be unit tested.
 */
public record ModConfig(Swap swap, Lunge lunge) {
  public static final int MAX_SLOT = 8;
  public static final int MAX_DELAY_TICKS = 40;
  public static final int MAX_SHOW_TICKS = 20;

  /** {@code showTicks} is how many ticks the target weapon stays selected after the hit. */
  public record Swap(boolean enabled, WeaponType target, int targetSlot, int showTicks) {}

  /** {@code showTicks} is how many ticks the spear stays selected after the jab. */
  public record Lunge(
      boolean enabled, int spearSlot, int carrierSlot, int delayTicks, int showTicks) {}

  public static ModConfig defaults() {
    return new ModConfig(new Swap(true, WeaponType.MACE, 8, 5), new Lunge(true, 1, -1, 0, 1));
  }

  /** Slot as shown in the UI: 0 means auto/held, 1-9 are hotbar slots. */
  public static int toUiSlot(int slot) {
    return slot + 1;
  }

  public static int fromUiSlot(int uiSlot) {
    return uiSlot - 1;
  }

  /** Clamps every value and fills anything missing from a hand-edited or partial file. */
  public ModConfig sanitized() {
    ModConfig defaults = defaults();
    Swap s = swap != null ? swap : defaults.swap;
    Lunge l = lunge != null ? lunge : defaults.lunge;
    return new ModConfig(
        new Swap(
            s.enabled,
            s.target != null ? s.target : defaults.swap.target,
            clamp(s.targetSlot, -1, MAX_SLOT),
            clamp(s.showTicks, 1, MAX_SHOW_TICKS)),
        new Lunge(
            l.enabled,
            clamp(l.spearSlot, -1, MAX_SLOT),
            clamp(l.carrierSlot, -1, MAX_SLOT),
            clamp(l.delayTicks, 0, MAX_DELAY_TICKS),
            clamp(l.showTicks, 1, MAX_SHOW_TICKS)));
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
