package io.github.ven0m0.autoattributes.client;

import io.github.ven0m0.autoattributes.AttackTiming;
import io.github.ven0m0.autoattributes.ModConfig;
import io.github.ven0m0.autoattributes.SlotResolver;
import io.github.ven0m0.autoattributes.WeaponType;
import io.github.ven0m0.autoattributes.client.mixin.MinecraftAccessor;
import io.github.ven0m0.autoattributes.client.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.HitResult;

/**
 * Runs at the start of each client tick, before vanilla handles input. Both the attribute swap and
 * the lunge select the target slot, flush the slot packet and attack inside one tick, then keep the
 * target selected for a few ticks before restoring the original slot, so the slot change is visible
 * to the server like a human swap rather than an instantaneous flicker. All tick bookkeeping lives
 * in {@link AttackTiming}; this class only reads and writes the real game state.
 */
public final class SwapController {
  /** Food level below which sprinting (and therefore lunging) is impossible. */
  private static final int MIN_LUNGE_FOOD = 7;

  private final KeyMapping swapKey;
  private final KeyMapping lungeKey;
  // Static so the first-person render mixin can ask whether an attack is being shown; only one
  // controller is ever created.
  private static final AttackTiming TIMING = new AttackTiming();

  public SwapController(KeyMapping swapKey, KeyMapping lungeKey) {
    this.swapKey = swapKey;
    this.lungeKey = lungeKey;
  }

  /** True while a swapped-in weapon is being held for display after its attack. */
  public static boolean showingAttack() {
    return TIMING.showing();
  }

  public void tick(Minecraft mc) {
    TIMING.nextTick();

    // Drain presses first so keys pressed while a screen is open do not fire later.
    boolean swapPressed = false;
    while (swapKey.consumeClick()) {
      swapPressed = true;
    }

    LocalPlayer player = mc.player;
    AttackTiming.Step step =
        TIMING.advance(player != null ? player.getInventory().getSelectedSlot() : -1);
    if (step.blocked()) {
      return;
    }
    if (step.restoreTo() >= 0) {
      player.getInventory().setSelectedSlot(step.restoreTo());
    }

    if (player == null || mc.gameMode == null || mc.gui.screen() != null || player.isUsingItem()) {
      return;
    }

    ModConfig config = ConfigStore.get();
    if (swapPressed && config.swap().enabled()) {
      runSwap(mc, player, config.swap());
      return;
    }

    if (config.lunge().enabled() && lungeKey.isDown()) {
      runLunge(mc, player, config.lunge());
    }
  }

  private void runSwap(Minecraft mc, LocalPlayer player, ModConfig.Swap swap) {
    if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) {
      return;
    }
    Inventory inv = player.getInventory();
    int held = inv.getSelectedSlot();
    int target =
        SlotResolver.resolve(
            swap.targetSlot(),
            i -> WeaponMatcher.matches(swap.target(), inv.getItem(i)),
            Inventory.getSelectionSize());
    if (target >= 0 && target != held) {
      showAttack(mc, inv, target, held, swap.showTicks());
    }
  }

  private void runLunge(Minecraft mc, LocalPlayer player, ModConfig.Lunge lunge) {
    Inventory inv = player.getInventory();
    int held = inv.getSelectedSlot();

    if (lunge.carrierSlot() >= 0 && lunge.carrierSlot() != held) {
      inv.setSelectedSlot(lunge.carrierSlot());
      return;
    }
    if (!TIMING.lungeReady(lunge.delayTicks())) {
      return;
    }
    if (player.getFoodData().getFoodLevel() < MIN_LUNGE_FOOD
        || player.isPassenger()
        || player.isFallFlying()
        || player.isInWater()) {
      return;
    }
    // The carrier's attack cooldown is what limits the lunge rate, not the spear's.
    if (player.cannotAttackWithItem(inv.getItem(held), 0)
        || ((MinecraftAccessor) mc).autoattributes$getMissTime() > 0) {
      return;
    }

    int spear =
        SlotResolver.resolveBest(
            lunge.spearSlot(), i -> spearScore(mc, inv.getItem(i)), Inventory.getSelectionSize());
    if (spear < 0) {
      return;
    }
    if (spear == held) {
      if (((MinecraftAccessor) mc).autoattributes$startAttack()) {
        TIMING.markLunge();
      }
    } else if (showAttack(mc, inv, spear, held, lunge.showTicks())) {
      TIMING.markLunge();
    }
  }

  /**
   * Selects {@code shown}, attacks in the same tick, and keeps it selected until {@code showTicks}
   * later. The attack itself still happens with the shown item held, so its enchantments apply. A
   * rejected attack restores the slot at once, since showing an item that did nothing only looks
   * odd.
   */
  private boolean showAttack(Minecraft mc, Inventory inv, int shown, int held, int showTicks) {
    inv.setSelectedSlot(shown);
    boolean accepted = false;
    try {
      ((MultiPlayerGameModeAccessor) mc.gameMode).autoattributes$ensureHasSentCarriedItem();
      accepted = ((MinecraftAccessor) mc).autoattributes$startAttack();
    } finally {
      if (accepted) {
        TIMING.showUntilRestore(shown, held, showTicks);
      } else {
        inv.setSelectedSlot(held);
      }
    }
    return accepted;
  }

  /** Lunge level for a spear (0 if unenchanted), -1 for anything that is not a spear. */
  private static int spearScore(Minecraft mc, ItemStack stack) {
    if (!WeaponMatcher.matches(WeaponType.SPEAR, stack)) {
      return -1;
    }
    return mc.level
        .registryAccess()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .get(Enchantments.LUNGE)
        .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
        .orElse(0);
  }
}
