package io.github.ven0m0.autoattributes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable config. Slots are 0-based here (1-9 in the UI). A {@code carrierSlot} of -1 means
 * "whatever slot is currently held".
 */
public record ModConfig(List<Combo> combos, Lunge lunge) {
  public static final int COMBO_COUNT = 3;
  public static final int MAX_DELAY_TICKS = 40;

  public record Combo(
      boolean enabled, WeaponType source, int sourceSlot, WeaponType target, int targetSlot) {}

  public record Lunge(boolean enabled, int spearSlot, int carrierSlot, int delayTicks) {}

  private static final Logger LOGGER = LoggerFactory.getLogger("autoattributes");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static volatile ModConfig current = defaults();

  public static ModConfig get() {
    return current;
  }

  public static ModConfig defaults() {
    return new ModConfig(
        List.of(
            new Combo(true, WeaponType.SWORD, 0, WeaponType.MACE, 8),
            new Combo(true, WeaponType.SPEAR, 1, WeaponType.MACE, 8),
            new Combo(true, WeaponType.AXE, 2, WeaponType.SWORD, 0)),
        new Lunge(true, 1, -1, 0));
  }

  /** Clamps every value and fills anything missing from a hand-edited or partial file. */
  public ModConfig sanitized() {
    ModConfig defaults = defaults();
    List<Combo> cleaned = new ArrayList<>(COMBO_COUNT);
    for (int i = 0; i < COMBO_COUNT; i++) {
      Combo fallback = defaults.combos.get(i);
      Combo c =
          combos != null && i < combos.size() && combos.get(i) != null ? combos.get(i) : fallback;
      cleaned.add(
          new Combo(
              c.enabled,
              c.source != null ? c.source : fallback.source,
              clamp(c.sourceSlot, 0, 8),
              c.target != null ? c.target : fallback.target,
              clamp(c.targetSlot, 0, 8)));
    }
    Lunge l = lunge != null ? lunge : defaults.lunge;
    return new ModConfig(
        List.copyOf(cleaned),
        new Lunge(
            l.enabled,
            clamp(l.spearSlot, 0, 8),
            clamp(l.carrierSlot, -1, 8),
            clamp(l.delayTicks, 0, MAX_DELAY_TICKS)));
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }

  private static Path path() {
    return FabricLoader.getInstance().getConfigDir().resolve("autoattributes.json");
  }

  public static void load() {
    Path p = path();
    if (!Files.exists(p)) {
      save(defaults());
      return;
    }
    try (Reader r = Files.newBufferedReader(p)) {
      ModConfig loaded = GSON.fromJson(r, ModConfig.class);
      current = (loaded != null ? loaded : defaults()).sanitized();
    } catch (IOException | JsonParseException e) {
      LOGGER.warn("Could not read {}, using defaults", p, e);
      current = defaults();
    }
  }

  public static void save(ModConfig config) {
    ModConfig clean = config.sanitized();
    current = clean;
    Path p = path();
    try (Writer w = Files.newBufferedWriter(p)) {
      GSON.toJson(clean, w);
    } catch (IOException e) {
      LOGGER.error("Could not write {}", p, e);
    }
  }
}
