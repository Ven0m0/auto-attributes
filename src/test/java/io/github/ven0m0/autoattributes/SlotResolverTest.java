package io.github.ven0m0.autoattributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlotResolverTest {
  @Test
  void configuredSlotWinsWhenItMatches() {
    assertEquals(8, SlotResolver.resolve(8, i -> i == 8 || i == 2, 9));
  }

  @Test
  void fallsBackToFirstMatchingSlot() {
    assertEquals(2, SlotResolver.resolve(8, i -> i == 2 || i == 5, 9));
  }

  @Test
  void returnsMinusOneWhenNothingMatches() {
    assertEquals(-1, SlotResolver.resolve(0, i -> false, 9));
  }

  @Test
  void outOfRangeConfiguredSlotStillScans() {
    assertEquals(3, SlotResolver.resolve(42, i -> i == 3, 9));
    assertEquals(3, SlotResolver.resolve(-1, i -> i == 3, 9));
  }
}
