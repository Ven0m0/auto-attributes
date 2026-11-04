package io.github.ven0m0.autoattributes.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.ven0m0.autoattributes.ModConfig;
import io.github.ven0m0.autoattributes.WeaponType;
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
    ModConfig current = ConfigStore.get();
    ModConfig.Swap swap = current.swap();
    boolean[] swapEnabled = {swap.enabled()};
    WeaponType[] swapTarget = {swap.target()};
    int[] swapSlot = {ModConfig.toUiSlot(swap.targetSlot())};
    int[] swapShowTicks = {swap.showTicks()};

    ModConfig.Lunge lunge = current.lunge();
    boolean[] lungeEnabled = {lunge.enabled()};
    int[] spearSlot = {ModConfig.toUiSlot(lunge.spearSlot())};
    int[] carrierSlot = {ModConfig.toUiSlot(lunge.carrierSlot())}; // 0 = currently held
    int[] delay = {lunge.delayTicks()};
    int[] showTicks = {lunge.showTicks()};

    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("autoattributes.config.title"))
            .setSavingRunnable(() ->
                ConfigStore.save(
                    new ModConfig(
                        new ModConfig.Swap(
                            swapEnabled[0],
                            swapTarget[0],
                            ModConfig.fromUiSlot(swapSlot[0]),
                            swapShowTicks[0]),
                        new ModConfig.Lunge(
                            lungeEnabled[0],
                            ModConfig.fromUiSlot(spearSlot[0]),
                            ModConfig.fromUiSlot(carrierSlot[0]),
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
                Component.translatable("autoattributes.config.target_slot"),
                swapSlot[0],
                0,
                ModConfig.toUiSlot(ModConfig.MAX_SLOT))
            .setDefaultValue(ModConfig.toUiSlot(swapDef.targetSlot()))
            .setSaveConsumer(v -> swapSlot[0] = v)
            .build());
    swapCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.swap_show_ticks"),
                swapShowTicks[0],
                1,
                ModConfig.MAX_SHOW_TICKS)
            .setDefaultValue(swapDef.showTicks())
            .setTooltip(Component.translatable("autoattributes.config.swap_show_ticks.tooltip"))
            .setSaveConsumer(v -> swapShowTicks[0] = v)
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
                Component.translatable("autoattributes.config.spear_slot"),
                spearSlot[0],
                0,
                ModConfig.toUiSlot(ModConfig.MAX_SLOT))
            .setDefaultValue(ModConfig.toUiSlot(def.spearSlot()))
            .setSaveConsumer(v -> spearSlot[0] = v)
            .build());
    lungeCat.addEntry(
        entries
            .startIntSlider(
                Component.translatable("autoattributes.config.carrier_slot"),
                carrierSlot[0],
                0,
                ModConfig.toUiSlot(ModConfig.MAX_SLOT))
            .setDefaultValue(ModConfig.toUiSlot(def.carrierSlot()))
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
