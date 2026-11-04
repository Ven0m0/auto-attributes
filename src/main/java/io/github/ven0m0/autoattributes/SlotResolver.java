package io.github.ven0m0.autoattributes;

import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/** Minecraft-free slot lookup so it can be unit tested. */
public final class SlotResolver {
  private SlotResolver() {}

  /**
   * Returns the configured slot if it matches, otherwise the first matching hotbar slot, otherwise
   * -1.
   *
   * @param configuredSlot 0-based slot from the config
   * @param matches true when the item in the given slot is the wanted type
   * @param hotbarSize number of hotbar slots to scan
   */
  public static int resolve(int configuredSlot, IntPredicate matches, int hotbarSize) {
    return resolveBest(configuredSlot, i -> matches.test(i) ? 0 : -1, hotbarSize);
  }

  /**
   * Like {@link #resolve} but the fallback scan picks the highest score instead of the first match;
   * ties go to the lowest slot. A negative score means the slot does not match at all. The
   * configured slot still wins whenever it matches, whatever it scores.
   */
  public static int resolveBest(int configuredSlot, IntUnaryOperator score, int hotbarSize) {
    if (configuredSlot >= 0
        && configuredSlot < hotbarSize
        && score.applyAsInt(configuredSlot) >= 0) {
      return configuredSlot;
    }
    int best = -1;
    int bestScore = -1;
    for (int i = 0; i < hotbarSize; i++) {
      int s = score.applyAsInt(i);
      if (s > bestScore) {
        best = i;
        bestScore = s;
      }
    }
    return best;
  }
}
