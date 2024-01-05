package io.github.aparx.perx.config;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.config.configs.DatabaseConfig;
import io.github.aparx.perx.config.configs.MessageConfig;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:32
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class ConfigManager {

  private static final BiFunction<File, String, ? extends Config> DEFAULT_YAML_FACTORY =
      (file, id) -> new PerxConfig(new File(file, id + ".yml"), id);

  private final Map<String, Config> configMap = new HashMap<>();
  private final File dataFolder;

  private final Function<String, ? extends Config> defaultFactory;

  private final DatabaseConfig databaseConfig = new DatabaseConfig(this);
  private final MessageConfig messageConfig = new MessageConfig(this);

  public ConfigManager(File dataFolder) {
    this(dataFolder, DEFAULT_YAML_FACTORY);
  }

  public ConfigManager(File dataFolder, BiFunction<File, String, ? extends Config> configFactory) {
    Preconditions.checkNotNull(dataFolder, "Data folder must not be null");
    Preconditions.checkNotNull(configFactory, "Config factory must not be null");
    this.dataFolder = dataFolder;
    this.defaultFactory = wrapFactoryInAssertiveFactory((id) -> {
      return configFactory.apply(getDataFolder(), id);
    });
  }

  public File getDataFolder() {
    return dataFolder;
  }

  public DatabaseConfig getDatabaseConfig() {
    return databaseConfig;
  }

  public MessageConfig getMessageConfig() {
    return messageConfig;
  }

  public void load() {
    databaseConfig.init(databaseConfig.getConfig());
    messageConfig.init(messageConfig.getConfig());
  }

  public Config getOrCreate(Class<? extends ConfigHandle> type) {
    Preconditions.checkNotNull(type, "Type must not be null");
    return getOrCreate(ConfigHandle.getIdFromType(type));
  }

  @CanIgnoreReturnValue
  public Config getOrCreate(String configId, Function<String, ? extends Config> factory) {
    Preconditions.checkNotNull(configId, "ID must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    return configMap.computeIfAbsent(configId, wrapFactoryInAssertiveFactory(factory));
  }

  @CanIgnoreReturnValue
  public Config getOrCreate(String configId) {
    Preconditions.checkNotNull(configId, "ID must not be null");
    return configMap.computeIfAbsent(configId, defaultFactory);
  }

  public boolean contains(String configId) {
    return configMap.containsKey(configId);
  }

  public boolean contains(Config config) {
    return config.equals(configMap.get(config.getId()));
  }

  private Function<String, ? extends Config>
  wrapFactoryInAssertiveFactory(Function<String, ? extends Config> factory) {
    return (key) -> {
      @Nullable Config config = factory.apply(key);
      Preconditions.checkNotNull(config, "Factory must not return null");
      return config;
    };
  }

}
