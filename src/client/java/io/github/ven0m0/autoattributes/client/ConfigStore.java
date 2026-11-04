package io.github.ven0m0.autoattributes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.github.ven0m0.autoattributes.ModConfig;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Holds the active config and reads/writes {@code config/autoattributes.json}. */
public final class ConfigStore {
  private static final Logger LOGGER = LoggerFactory.getLogger("autoattributes");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static volatile ModConfig current = ModConfig.defaults();

  private ConfigStore() {}

  public static ModConfig get() {
    return current;
  }

  private static Path path() {
    return FabricLoader.getInstance().getConfigDir().resolve("autoattributes.json");
  }

  public static void load() {
    Path p = path();
    if (!Files.exists(p)) {
      save(ModConfig.defaults());
      return;
    }
    try (Reader r = Files.newBufferedReader(p)) {
      ModConfig loaded = GSON.fromJson(r, ModConfig.class);
      current = (loaded != null ? loaded : ModConfig.defaults()).sanitized();
    } catch (IOException | JsonParseException e) {
      LOGGER.warn("Could not read {}, using defaults", p, e);
      current = ModConfig.defaults();
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
