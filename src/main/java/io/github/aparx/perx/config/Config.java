package io.github.aparx.perx.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Deterministic;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 02:32
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface Config extends ConfigFacade {

  @Deterministic
  String getId();

  File getFile();

  void save();

  void load();

  FileConfiguration getConfig();

  @CanIgnoreReturnValue
  default <T> T set(String path, T value, String... comments) {
    FileConfiguration config = getConfig();
    config.set(path, value);
    if (ArrayUtils.isNotEmpty(comments))
      config.setComments(path, List.of(comments));
    return value;
  }

  @CanIgnoreReturnValue
  default <T> T set(ArrayPath path, T value, String... comments) {
    return set(path.join(), value, comments);
  }

  @SuppressWarnings("unchecked")
  @CanIgnoreReturnValue
  default <T> @Nullable T setIfAbsent(String path, T value, String... comments) {
    FileConfiguration config = getConfig();
    if (!config.contains(path))
      return set(path, value, comments);
    return (T) config.get(path);
  }

  default <T> @Nullable T setIfAbsent(ArrayPath path, T value, String... comments) {
    return setIfAbsent(path.join(), value, comments);
  }

  default @Nullable Object get(String path) {
    return getConfig().get(path);
  }

  default @Nullable Object get(ArrayPath path) {
    return getConfig().get(path.join());
  }

}
