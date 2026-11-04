package io.github.ven0m0.autoattributes.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
  @Invoker("startAttack")
  boolean autoattributes$startAttack();

  @Accessor("missTime")
  int autoattributes$getMissTime();
}
