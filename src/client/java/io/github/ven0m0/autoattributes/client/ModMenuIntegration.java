package io.github.ven0m0.autoattributes.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.util.ArrayList;
import java.util.List;
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

  /** Mutable UI-only state; turned into an immutable {@link ModConfig} on save. */
  private static final class ComboDraft {
    boolean enabled;
    WeaponType source;
    int sourceSlot;
    WeaponType target;
    int targetSlot;

    ComboDraft(ModConfig.Combo c) {
      enabled = c.enabled();
      source = c.source();
      sourceSlot = c.sourceSlot() + 1;
      target = c.target();
      targetSlot = c.targetSlot() + 1;
    }

    ModConfig.Combo toCombo() {
      return new ModConfig.Combo(enabled, source, sourceSlot - 1, target, targetSlot - 1);
    }
  }

  private static Screen buildScreen(Screen parent) {
    ModConfig current = ModConfig.get();
    List<ComboDraft> drafts = new ArrayList<>();
    for (ModConfig.Combo c : current.combos()) {
      drafts.add(new ComboDraft(c));
    }
    ModConfig.Lunge lunge = current.lunge();
    boolean[] lungeEnabled = {lunge.enabled()};
    int[] spearSlot = {lunge.spearSlot() + 1};
    int[] carrierSlot = {lunge.carrierSlot() + 1}; // 0 = currently held
    int[] delay = {lunge.delayTicks()};

    ConfigBuilder builder =
        ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("autoattributes.config.title"))
            .setSavingRunnable(() -> {
              List<ModConfig.Combo> combos = new ArrayList<>();
              for (ComboDraft d : drafts) {
                combos.add(d.toCombo());
              }
              ModConfig.save(
                  new ModConfig(
                      List.copyOf(combos),
                      new ModConfig.Lunge(
                          lungeEnabled[0], spearSlot[0] - 1, carrierSlot[0] - 1, delay[0])));
            });
    ConfigEntryBuilder entries = builder.entryBuilder();

    for (int i = 0; i < drafts.size(); i++) {
      ComboDraft d = drafts.get(i);
      ModConfig.Combo def = ModConfig.defaults().combos().get(i);
      ConfigCategory cat =
          builder.getOrCreateCategory(Component.translatable("autoattributes.config.combo", i + 1));
      cat.addEntry(
          entries
              .startBooleanToggle(
                  Component.translatable("autoattributes.config.enabled"), d.enabled)
              .setDefaultValue(def.enabled())
              .setSaveConsumer(v -> d.enabled = v)
              .build());
      cat.addEntry(
          entries
              .startEnumSelector(
                  Component.translatable("autoattributes.config.source"),
                  WeaponType.class,
                  d.source)
              .setDefaultValue(def.source())
              .setEnumNameProvider(ModMenuIntegration::weaponName)
              .setSaveConsumer(v -> d.source = v)
              .build());
      cat.addEntry(
          entries
              .startIntSlider(
                  Component.translatable("autoattributes.config.source_slot"), d.sourceSlot, 1, 9)
              .setDefaultValue(def.sourceSlot() + 1)
              .setSaveConsumer(v -> d.sourceSlot = v)
              .build());
      cat.addEntry(
          entries
              .startEnumSelector(
                  Component.translatable("autoattributes.config.target"),
                  WeaponType.class,
                  d.target)
              .setDefaultValue(def.target())
              .setEnumNameProvider(ModMenuIntegration::weaponName)
              .setSaveConsumer(v -> d.target = v)
              .build());
      cat.addEntry(
          entries
              .startIntSlider(
                  Component.translatable("autoattributes.config.target_slot"), d.targetSlot, 1, 9)
              .setDefaultValue(def.targetSlot() + 1)
              .setSaveConsumer(v -> d.targetSlot = v)
              .build());
    }

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
                Component.translatable("autoattributes.config.spear_slot"), spearSlot[0], 1, 9)
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

    return builder.build();
  }

  private static Component weaponName(Enum<?> e) {
    return Component.translatable("autoattributes.weapon." + e.name().toLowerCase(Locale.ROOT));
  }
}
