package io.github.ven0m0.autoattributes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class AutoAttributesClient implements ClientModInitializer {
  public static final String MOD_ID = "autoattributes";

  @Override
  public void onInitializeClient() {
    ModConfig.load();

    KeyMapping.Category category =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    KeyMapping swapKey = register("key." + MOD_ID + ".swap", category);
    KeyMapping lungeKey = register("key." + MOD_ID + ".lunge", category);

    SwapController controller = new SwapController(swapKey, lungeKey);
    ClientTickEvents.START_CLIENT_TICK.register(controller::tick);
  }

  /** Keys start unbound; players bind them in the vanilla Controls screen. */
  private static KeyMapping register(String translationKey, KeyMapping.Category category) {
    return KeyMappingHelper.registerKeyMapping(
        new KeyMapping(
            translationKey,
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            category));
  }
}
