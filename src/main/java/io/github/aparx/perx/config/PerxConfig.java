package io.github.aparx.perx.config;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.utils.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:27
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxConfig implements Config {

  private final File file;
  private final String configId;

  private @Nullable FileConfiguration config;

  public PerxConfig(File file, String configId) {
    Preconditions.checkNotNull(file, "File must not be null");
    Preconditions.checkNotNull(configId, "ID must not be null");
    this.file = file;
    this.configId = configId;
  }

  @Override
  public String getId() {
    return configId;
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public void save() {
    Preconditions.checkState(config != null, "Config is not loaded");
    try {
      config.save(getFile());
    } catch (IOException e) {
      Perx.getLogger().log(Level.WARNING, "Error on config save", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void load() {
    FileUtils.createFileIfNotExists(getFile());
    if (this.config == null)
      this.config = new YamlConfiguration();
    try {
      this.config.load(getFile());
    } catch (IOException | InvalidConfigurationException e) {
      Perx.getLogger().log(Level.WARNING, "Error on config load", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public FileConfiguration getConfig() {
    @Nullable FileConfiguration config = this.config;
    Preconditions.checkState(config != null, "Config is not loaded");
    return config;
  }

}
