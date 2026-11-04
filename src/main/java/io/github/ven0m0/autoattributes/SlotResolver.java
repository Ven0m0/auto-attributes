package io.github.ven0m0.autoattributes;

import java.util.function.IntPredicate;

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
    if (configuredSlot >= 0 && configuredSlot < hotbarSize && matches.test(configuredSlot)) {
      return configuredSlot;
    }
    for (int i = 0; i < hotbarSize; i++) {
      if (matches.test(i)) {
        return i;
      }
    }
    return -1;
  }
}
