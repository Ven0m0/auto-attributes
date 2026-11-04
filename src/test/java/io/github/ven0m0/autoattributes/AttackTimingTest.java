package io.github.ven0m0.autoattributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttackTimingTest {
  @Test
  void idleWhenNothingIsShown() {
    AttackTiming t = new AttackTiming();
    t.nextTick();
    assertEquals(new AttackTiming.Step(false, -1), t.advance(3));
    assertFalse(t.showing());
  }

  @Test
  void blocksUntilShowTicksPassThenRestores() {
    AttackTiming t = new AttackTiming();
    t.nextTick();
    t.showUntilRestore(8, 2, 2);
    assertTrue(t.showing());
    t.nextTick();
    assertTrue(t.advance(8).blocked());
    t.nextTick();
    assertEquals(new AttackTiming.Step(false, 2), t.advance(8));
    assertFalse(t.showing());
    assertEquals(new AttackTiming.Step(false, -1), t.advance(2));
  }

  @Test
  void doesNotRestoreWhenPlayerScrolledAway() {
    AttackTiming t = new AttackTiming();
    t.nextTick();
    t.showUntilRestore(8, 2, 1);
    t.nextTick();
    assertEquals(new AttackTiming.Step(false, -1), t.advance(5));
    assertFalse(t.showing());
    assertFalse(t.advance(8).blocked());
  }

  @Test
  void lungeRateFollowsDelay() {
    AttackTiming t = new AttackTiming();
    t.nextTick();
    assertTrue(t.lungeReady(40));
    t.markLunge();
    t.nextTick();
    assertTrue(t.lungeReady(0));
    assertFalse(t.lungeReady(1));
    t.nextTick();
    assertTrue(t.lungeReady(1));
  }
}
