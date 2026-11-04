package io.github.ven0m0.autoattributes.client;

import io.github.ven0m0.autoattributes.SlotResolver;
import io.github.ven0m0.autoattributes.client.mixin.MinecraftAccessor;
import io.github.ven0m0.autoattributes.client.mixin.MultiPlayerGameModeAccessor;
import java.util.List;
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
 * Runs at the start of each client tick, before vanilla handles input. A swap-attack selects the
 * target slot, flushes the slot packet, attacks, and selects the original slot again, all inside
 * one tick, so the server applies the target item's enchantments to the original item's attack.
 */
public final class SwapController {
  /** Food level below which sprinting (and therefore lunging) is impossible. */
  private static final int MIN_LUNGE_FOOD = 7;

  private final List<KeyMapping> comboKeys;
  private final KeyMapping lungeKey;
  private long tickCount;
  private long lastLungeTick = Long.MIN_VALUE / 2;

  public SwapController(List<KeyMapping> comboKeys, KeyMapping lungeKey) {
    this.comboKeys = List.copyOf(comboKeys);
    this.lungeKey = lungeKey;
  }

  public void tick(Minecraft mc) {
    tickCount++;

    // Drain presses first so keys pressed while a screen is open do not fire later.
    boolean[] comboPressed = new boolean[comboKeys.size()];
    for (int i = 0; i < comboKeys.size(); i++) {
      while (comboKeys.get(i).consumeClick()) {
        comboPressed[i] = true;
      }
    }

    LocalPlayer player = mc.player;
    if (player == null || mc.gameMode == null || mc.gui.screen() != null || player.isUsingItem()) {
      return;
    }

    ModConfig config = ModConfig.get();
    for (int i = 0; i < comboPressed.length; i++) {
      if (comboPressed[i] && config.combos().get(i).enabled()) {
        runCombo(mc, player, config.combos().get(i));
        return;
      }
    }

    if (config.lunge().enabled() && lungeKey.isDown()) {
      runLunge(mc, player, config.lunge());
    }
  }

  private void runCombo(Minecraft mc, LocalPlayer player, ModConfig.Combo combo) {
    Inventory inv = player.getInventory();
    int held = inv.getSelectedSlot();

    if (!combo.source().matches(inv.getItem(held))) {
      // Not holding the source weapon yet: this press only equips it, because switching resets
      // charge.
      int source =
          SlotResolver.resolve(
              combo.sourceSlot(),
              i -> combo.source().matches(inv.getItem(i)),
              Inventory.getSelectionSize());
      if (source >= 0) {
        inv.setSelectedSlot(source);
      }
      return;
    }

    if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.ENTITY) {
      return;
    }
    int target =
        SlotResolver.resolve(
            combo.targetSlot(),
            i -> combo.target().matches(inv.getItem(i)),
            Inventory.getSelectionSize());
    if (target >= 0) {
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
    // Only a jab that vanilla actually started restarts the delay window.
    if (swapAttack(mc, inv, spear, held)) {
      lastLungeTick = tickCount;
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
