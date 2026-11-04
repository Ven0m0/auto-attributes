package io.github.ven0m0.autoattributes.client.mixin;

import io.github.ven0m0.autoattributes.client.SwapController;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {
  @Shadow private ItemStack mainHandItem;
  @Shadow private float mainHandHeight;
  @Shadow private float oMainHandHeight;

  // Vanilla only draws a new item after the old one is fully lowered (about 3 ticks) and then
  // raises it slowly, so a short show would never be visible in first person.
  @Inject(method = "tick", at = @At("TAIL"))
  void autoattributes$showSwappedItem(LocalPlayer player, CallbackInfo ci) {
    if (SwapController.showingAttack()) {
      mainHandItem = player.getMainHandItem();
      mainHandHeight = 1.0F;
      oMainHandHeight = 1.0F;
    }
  }
}
