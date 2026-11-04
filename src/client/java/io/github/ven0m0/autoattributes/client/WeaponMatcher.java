package io.github.ven0m0.autoattributes.client;

import io.github.ven0m0.autoattributes.WeaponType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Maps the Minecraft-free {@link WeaponType} onto real items. */
final class WeaponMatcher {
  private WeaponMatcher() {}

  static boolean matches(WeaponType type, ItemStack stack) {
    return switch (type) {
      case SWORD -> stack.is(ItemTags.SWORDS);
      case AXE -> stack.is(ItemTags.AXES);
      case SPEAR -> stack.is(ItemTags.SPEARS);
      case MACE -> stack.is(Items.MACE);
    };
  }
}
