package io.github.ven0m0.autoattributes.client;

import io.github.ven0m0.autoattributes.SlotResolver;
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
 * Runs at the start of each client tick, before vanilla handles input. The attribute swap selects
 * the target slot, flushes the slot packet, attacks, and selects the original slot again, all
 * inside one tick, so the server applies the target item's enchantments to the held item's attack.
 * The lunge instead keeps the spear selected for a few ticks after the jab, so the slot change is
 * visible to the server like a human swap rather than an instantaneous flicker.
 */
public final class SwapController {
  /** Food level below which sprinting (and therefore lunging) is impossible. */
  private static final int MIN_LUNGE_FOOD = 7;

  private final KeyMapping swapKey;
  private final KeyMapping lungeKey;
  private long tickCount;
  private long lastLungeTick = Long.MIN_VALUE / 2;
  // Pending lunge restore; restoreSlot < 0 means none.
  private int restoreSlot = -1;
  private int spearShownSlot;
  private long restoreAtTick;

  public SwapController(KeyMapping swapKey, KeyMapping lungeKey) {
    this.swapKey = swapKey;
    this.lungeKey = lungeKey;
  }

  public void tick(Minecraft mc) {
    tickCount++;

    // Drain presses first so keys pressed while a screen is open do not fire later.
    boolean swapPressed = false;
    while (swapKey.consumeClick()) {
      swapPressed = true;
    }

    LocalPlayer player = mc.player;
    if (restoreSlot >= 0) {
      if (tickCount < restoreAtTick) {
        return;
      }
      // Skip the restore if the player scrolled away meanwhile; do not fight manual input.
      if (player != null && player.getInventory().getSelectedSlot() == spearShownSlot) {
        player.getInventory().setSelectedSlot(restoreSlot);
      }
      restoreSlot = -1;
    }

    if (player == null || mc.gameMode == null || mc.gui.screen() != null || player.isUsingItem()) {
      return;
    }

    ModConfig config = ModConfig.get();
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
            i -> swap.target().matches(inv.getItem(i)),
            Inventory.getSelectionSize());
    if (target >= 0 && target != held) {
      swapAttack(mc, inv, target, held);
    }
  }

  private void runLunge(Minecraft mc, LocalPlayer player, ModConfig.Lunge lunge) {
    Inventory inv = player.getInventory();
    int held = inv.getSelectedSlot();

    if (lunge.carrierSlot() >= 0 && lunge.carrierSlot() != held) {
      inv.setSelectedSlot(lunge.carrierSlot());
      return;
    }
    if (tickCount - lastLungeTick < lunge.delayTicks() + 1L) {
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

    int spear = findSpear(mc, inv, lunge.spearSlot());
    if (spear < 0) {
      return;
    }
    lungeAttack(mc, inv, spear, held, lunge.showTicks());
  }

  /**
   * Attacks with the spear and leaves it selected until a later tick. A jab vanilla rejected is
   * restored at once, since showing a spear that did nothing only looks odd.
   */
  private void lungeAttack(Minecraft mc, Inventory inv, int spear, int held, int showTicks) {
    if (spear == held) {
      if (((MinecraftAccessor) mc).autoattributes$startAttack()) {
        lastLungeTick = tickCount;
      }
      return;
    }
    inv.setSelectedSlot(spear);
    boolean accepted = false;
    try {
      ((MultiPlayerGameModeAccessor) mc.gameMode).autoattributes$ensureHasSentCarriedItem();
      accepted = ((MinecraftAccessor) mc).autoattributes$startAttack();
    } finally {
      if (accepted) {
        lastLungeTick = tickCount;
        restoreSlot = held;
        spearShownSlot = spear;
        restoreAtTick = tickCount + showTicks;
      } else {
        inv.setSelectedSlot(held);
      }
    }
  }

  /**
   * Configured slot if it holds a spear, otherwise the hotbar spear with the highest Lunge level.
   */
  private static int findSpear(Minecraft mc, Inventory inv, int configuredSlot) {
    int size = Inventory.getSelectionSize();
    if (configuredSlot >= 0
        && configuredSlot < size
        && WeaponType.SPEAR.matches(inv.getItem(configuredSlot))) {
      return configuredSlot;
    }
    int best = -1;
    int bestLevel = -1;
    for (int i = 0; i < size; i++) {
      ItemStack stack = inv.getItem(i);
      if (WeaponType.SPEAR.matches(stack)) {
        int level = lungeLevel(mc, stack);
        if (level > bestLevel) {
          best = i;
          bestLevel = level;
        }
      }
    }
    return best;
  }

  private static int lungeLevel(Minecraft mc, ItemStack stack) {
    return mc.level
        .registryAccess()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .get(Enchantments.LUNGE)
        .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
        .orElse(0);
  }

  /** Returns whether vanilla accepted the attack. */
  private static boolean swapAttack(Minecraft mc, Inventory inv, int targetSlot, int originalSlot) {
    inv.setSelectedSlot(targetSlot);
    try {
      ((MultiPlayerGameModeAccessor) mc.gameMode).autoattributes$ensureHasSentCarriedItem();
      return ((MinecraftAccessor) mc).autoattributes$startAttack();
    } finally {
      inv.setSelectedSlot(originalSlot);
    }
  }
}
