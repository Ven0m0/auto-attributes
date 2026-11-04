package io.github.ven0m0.autoattributes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Immutable config. Slots are 0-based here (1-9 in the UI). A {@code carrierSlot} of -1 means
 * "whatever slot is currently held". A {@code targetSlot} or {@code spearSlot} of -1 means
 * "auto-detect by scanning the hotbar".
 */
public record ModConfig(Swap swap, Lunge lunge) {
  public static final int MAX_DELAY_TICKS = 40;
  public static final int MAX_SHOW_TICKS = 10;

  public record Swap(boolean enabled, WeaponType target, int targetSlot) {}

  /** {@code showTicks} is how many ticks the spear stays selected after the jab. */
  public record Lunge(
      boolean enabled, int spearSlot, int carrierSlot, int delayTicks, int showTicks) {}

  private static final Logger LOGGER = LoggerFactory.getLogger("autoattributes");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static volatile ModConfig current = defaults();

  public static ModConfig get() {
    return current;
  }

  public static ModConfig defaults() {
    return new ModConfig(new Swap(true, WeaponType.MACE, 8), new Lunge(true, 1, -1, 0, 1));
  }

  /** Clamps every value and fills anything missing from a hand-edited or partial file. */
  public ModConfig sanitized() {
    ModConfig defaults = defaults();
    Swap s = swap != null ? swap : defaults.swap;
    Lunge l = lunge != null ? lunge : defaults.lunge;
    return new ModConfig(
        new Swap(
            s.enabled,
            s.target != null ? s.target : defaults.swap.target,
            clamp(s.targetSlot, -1, 8)),
        new Lunge(
            l.enabled,
            clamp(l.spearSlot, -1, 8),
            clamp(l.carrierSlot, -1, 8),
            clamp(l.delayTicks, 0, MAX_DELAY_TICKS),
            clamp(l.showTicks, 1, MAX_SHOW_TICKS)));
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
