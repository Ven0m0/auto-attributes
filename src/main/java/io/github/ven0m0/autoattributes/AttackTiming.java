package io.github.ven0m0.autoattributes;

/**
 * Minecraft-free tick bookkeeping for the show-then-restore attack and the lunge rate limit. The
 * caller owns the real inventory and only asks this class what to do each tick.
 */
public final class AttackTiming {
  /** Outcome of {@link #advance}: {@code restoreTo < 0} means leave the selection alone. */
  public record Step(boolean blocked, int restoreTo) {}

  private static final Step IDLE = new Step(false, -1);
  private static final Step BLOCKED = new Step(true, -1);

  private long tick;
  // Far enough in the past that the first lunge is never rate limited.
  private long lastLungeTick = Long.MIN_VALUE / 2;
  // restoreSlot < 0 means no restore is pending.
  private int restoreSlot = -1;
  private int shownSlot;
  private long restoreAtTick;

  /** Advances the clock; call once at the start of every client tick. */
  public void nextTick() {
    tick++;
  }

  /**
   * While a shown item is still on display the step is blocked, so no new attack starts on top of
   * it. Once due, the original slot is returned only if the player still has the shown item
   * selected, so a manual scroll in the meantime is never fought.
   */
  public Step advance(int selectedSlot) {
    if (restoreSlot < 0) {
      return IDLE;
    }
    if (tick < restoreAtTick) {
      return BLOCKED;
    }
    int target = selectedSlot == shownSlot ? restoreSlot : -1;
    restoreSlot = -1;
    return target < 0 ? IDLE : new Step(false, target);
  }

  /** Records that {@code shown} was selected for an accepted attack from {@code heldSlot}. */
  public void showUntilRestore(int shown, int heldSlot, int showTicks) {
    restoreSlot = heldSlot;
    shownSlot = shown;
    restoreAtTick = tick + showTicks;
  }

  public void markLunge() {
    lastLungeTick = tick;
  }

  /** True once {@code delayTicks} extra ticks have passed since the last lunge. */
  public boolean lungeReady(int delayTicks) {
    return tick - lastLungeTick >= delayTicks + 1L;
  }
}
