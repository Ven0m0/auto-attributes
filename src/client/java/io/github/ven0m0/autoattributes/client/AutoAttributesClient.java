package io.github.ven0m0.autoattributes.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
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

    List<KeyMapping> comboKeys = new ArrayList<>(ModConfig.COMBO_COUNT);
    for (int i = 1; i <= ModConfig.COMBO_COUNT; i++) {
      comboKeys.add(register("key." + MOD_ID + ".combo" + i, category));
    }
    KeyMapping lungeKey = register("key." + MOD_ID + ".lunge", category);

    SwapController controller = new SwapController(comboKeys, lungeKey);
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
