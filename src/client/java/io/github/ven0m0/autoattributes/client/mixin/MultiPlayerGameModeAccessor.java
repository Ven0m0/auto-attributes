package io.github.ven0m0.autoattributes.client.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
  /** Flushes the pending selected-slot packet so the server sees the swap before the attack. */
  @Invoker("ensureHasSentCarriedItem")
  void autoattributes$ensureHasSentCarriedItem();
}
