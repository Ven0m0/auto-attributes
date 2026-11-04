package io.github.ven0m0.autoattributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ModConfigTest {
  @Test
  void defaultsSurviveSanitizing() {
    assertEquals(ModConfig.defaults(), ModConfig.defaults().sanitized());
  }

  @Test
  void clampsHandEditedValues() {
    ModConfig wild =
        new ModConfig(
            new ModConfig.Swap(true, WeaponType.AXE, 99, 0),
            new ModConfig.Lunge(true, -5, 42, 1000, 1000));
    ModConfig clean = wild.sanitized();
    assertEquals(ModConfig.MAX_SLOT, clean.swap().targetSlot());
    assertEquals(1, clean.swap().showTicks());
    assertEquals(-1, clean.lunge().spearSlot());
    assertEquals(ModConfig.MAX_SLOT, clean.lunge().carrierSlot());
    assertEquals(ModConfig.MAX_DELAY_TICKS, clean.lunge().delayTicks());
    assertEquals(ModConfig.MAX_SHOW_TICKS, clean.lunge().showTicks());
  }

  @Test
  void missingSectionsAndTargetFallBackToDefaults() {
    ModConfig defaults = ModConfig.defaults();
    ModConfig clean = new ModConfig(new ModConfig.Swap(false, null, 0, 1), null).sanitized();
    assertEquals(defaults.swap().target(), clean.swap().target());
    assertEquals(defaults.lunge(), clean.lunge());
  }

  @Test
  void uiSlotsAreOneBased() {
    assertEquals(0, ModConfig.toUiSlot(-1));
    assertEquals(-1, ModConfig.fromUiSlot(0));
    assertEquals(8, ModConfig.fromUiSlot(9));
  }
}
