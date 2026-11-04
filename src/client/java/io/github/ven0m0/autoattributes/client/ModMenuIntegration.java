package io.github.ven0m0.autoattributes.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.util.Locale;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return ModMenuIntegration::buildScreen;
  }

  private static Screen buildScreen(Screen parent) {
    ModConfig current = ModConfig.get();
    ModConfig.Swap swap = current.swap();
    boolean[] swapEnabled = {swap.enabled()};
    WeaponType[] swapTarget = {swap.target()};
    int[] swapSlot = {swap.targetSlot() + 1};

    ModConfig.Lunge lunge = current.lunge();
    boolean[] lungeEnabled = {lunge.enabled()};
    int[] spearSlot = {lunge.spearSlot() + 1};
    int[] carrierSlot = {lunge.carrierSlot() + 1}; // 0 = currently held
    int[] delay = {lunge.delayTicks()};
    int[] showTicks = {lunge.showTicks()};

    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("autoattributes.config.title"))
            .setSavingRunnable(() ->
                ModConfig.save(
                    new ModConfig(
                        new ModConfig.Swap(swapEnabled[0], swapTarget[0], swapSlot[0] - 1),
                        new ModConfig.Lunge(
                            lungeEnabled[0],
                            spearSlot[0] - 1,
                            carrierSlot[0] - 1,
                            delay[0],
                            showTicks[0]))));
    ConfigEntryBuilder entries = builder.entryBuilder();

    ModConfig.Swap swapDef = ModConfig.defaults().swap();
    ConfigCategory swapCat =
        builder.getOrCreateCategory(Component.translatable("autoattributes.config.swap"));
    swapCat.addEntry(
        entries
            .startBooleanToggle(
                Component.translatable("autoattributes.config.enabled"), swapEnabled[0])
            .setDefaultValue(swapDef.enabled())
            .setSaveConsumer(v -> swapEnabled[0] = v)
            .build());
    swapCat.addEntry(
        entries
            .startEnumSelector(
                Component.translatable("autoattributes.config.target"),
                WeaponType.class,
                swapTarget[0])
            .setDefaultValue(swapDef.target())
            .setEnumNameProvider(ModMenuIntegration::weaponName)
            .setSaveConsumer(v -> swapTarget[0] = v)
            .build());
    swapCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.target_slot"), swapSlot[0], 0, 9)
            .setDefaultValue(swapDef.targetSlot() + 1)
            .setSaveConsumer(v -> swapSlot[0] = v)
            .build());

    ModConfig.Lunge def = ModConfig.defaults().lunge();
    ConfigCategory lungeCat =
        builder.getOrCreateCategory(Component.translatable("autoattributes.config.lunge"));
    lungeCat.addEntry(
        entries
            .startBooleanToggle(
                Component.translatable("autoattributes.config.enabled"), lungeEnabled[0])
            .setDefaultValue(def.enabled())
            .setSaveConsumer(v -> lungeEnabled[0] = v)
            .build());
    lungeCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.spear_slot"), spearSlot[0], 0, 9)
            .setDefaultValue(def.spearSlot() + 1)
            .setSaveConsumer(v -> spearSlot[0] = v)
            .build());
    lungeCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.carrier_slot"), carrierSlot[0], 0, 9)
            .setDefaultValue(def.carrierSlot() + 1)
            .setSaveConsumer(v -> carrierSlot[0] = v)
            .build());
    lungeCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.delay"),
                delay[0],
                0,
                ModConfig.MAX_DELAY_TICKS)
            .setDefaultValue(def.delayTicks())
            .setTooltip(Component.translatable("autoattributes.config.delay.tooltip"))
            .setSaveConsumer(v -> delay[0] = v)
            .build());
    lungeCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.show_ticks"),
                showTicks[0],
                1,
                ModConfig.MAX_SHOW_TICKS)
            .setDefaultValue(def.showTicks())
            .setTooltip(Component.translatable("autoattributes.config.show_ticks.tooltip"))
            .setSaveConsumer(v -> showTicks[0] = v)
            .build());

    return builder.build();
  }

  private static Component weaponName(Enum<?> e) {
    return Component.translatable("autoattributes.weapon." + e.name().toLowerCase(Locale.ROOT));
  }
}
