package io.github.aparx.perx.config;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:42
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public abstract class ConfigHandle implements ConfigFacade {

  private final String configId;
  private final Function<ConfigHandle, Config> configResolver;

  public ConfigHandle(ConfigManager configManager) {
    this((handle) -> configManager.getOrCreate(handle.getConfigId()));
  }

  public ConfigHandle(Function<ConfigHandle, Config> configResolver) {
    this.configId = getIdFromType(getClass());
    this.configResolver = createCyclicPreventiveResolver(configResolver);
  }

  public ConfigHandle(String configId, Function<ConfigHandle, Config> configResolver) {
    Preconditions.checkNotNull(configId, "Config ID must not be null");
    this.configId = configId;
    this.configResolver = createCyclicPreventiveResolver(configResolver);
  }

  public static String getIdFromType(Class<? extends ConfigHandle> type) {
    ConfigHandleId annotation = type.getAnnotation(ConfigHandleId.class);
    Preconditions.checkNotNull(annotation, "Missing ConfigHandleId annotation");
    return annotation.value();
  }

  private static Function<ConfigHandle, Config> createCyclicPreventiveResolver(
      Function<ConfigHandle, Config> resolver) {
    Preconditions.checkNotNull(resolver, "Resolver must not be null");
    AtomicBoolean cycler = new AtomicBoolean();
    return (handle) -> {
      try {
        boolean value = cycler.get();
        Preconditions.checkState(!value, "Cyclic config resolve");
        cycler.set(true);
        return resolver.apply(handle);
      } finally {
        cycler.set(false);
      }
    };
  }

  public abstract void init(Config config);

  public final Config getConfig() {
    return configResolver.apply(this);
  }

  public String getConfigId() {
    return configId;
  }

  @Override
  public <T> T set(String path, T value, String... comments) {
    return getConfig().set(path, value, comments);
  }

  @Override
  public <T> T set(ArrayPath path, T value, String... comments) {
    return getConfig().set(path, value, comments);
  }

  @Override
  public <T> @Nullable T setIfAbsent(String path, T value, String... comments) {
    return getConfig().setIfAbsent(path, value, comments);
  }

  @Override
  public <T> @Nullable T setIfAbsent(ArrayPath path, T value, String... comments) {
    return getConfig().setIfAbsent(path, value, comments);
  }

  @Override
  public @Nullable Object get(String path) {
    return getConfig().get(path);
  }

  @Override
  public @Nullable Object get(ArrayPath path) {
    return getConfig().get(path);
  }

  @Override
  public Object getRequired(ArrayPath path) {
    return getConfig().getRequired(path);
  }

  @Override
  public @Nullable String getString(String path) {
    return getConfig().getString(path);
  }

  @Override
  public @Nullable String getString(ArrayPath path) {
    return getConfig().getString(path);
  }

  @Override
  public String getRequiredString(ArrayPath path) {
    return getConfig().getRequiredString(path);
  }
}
