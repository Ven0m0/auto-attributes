package io.github.ven0m0.autoattributes.client;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum WeaponType {
  SWORD,
  AXE,
  SPEAR,
  MACE;

  public boolean matches(ItemStack stack) {
    return switch (this) {
      case SWORD -> stack.is(ItemTags.SWORDS);
      case AXE -> stack.is(ItemTags.AXES);
      case SPEAR -> stack.is(ItemTags.SPEARS);
      case MACE -> stack.is(Items.MACE);
    };
  }
}
