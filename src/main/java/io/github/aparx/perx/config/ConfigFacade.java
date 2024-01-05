package io.github.aparx.perx.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:02
 * @since 1.0
 */
public interface ConfigFacade {

  @CanIgnoreReturnValue
  <T> T set(String path, T value, String... comments);

  @CanIgnoreReturnValue
  <T> T set(ArrayPath path, T value, String... comments);

  @CanIgnoreReturnValue
  <T> @Nullable T setIfAbsent(String path, T value, String... comments);

  @CanIgnoreReturnValue
  <T> @Nullable T setIfAbsent(ArrayPath path, T value, String... comments);

  @Nullable Object get(String path);

  @Nullable Object get(ArrayPath path);

  default Object getRequired(ArrayPath path) {
    Object o = get(path);
    if (o == null)
      throw new NullPointerException("Missing value at " + path);
    return o;
  }

  default @Nullable String getString(String path) {
    return Objects.toString(get(path), null);
  }

  default @Nullable String getString(ArrayPath path) {
    return Objects.toString(get(path), null);
  }

  default String getRequiredString(ArrayPath path) {
    String string = Objects.toString(get(path), null);
    if (string == null)
      throw new NullPointerException("Missing string at " + path);
    return string;
  }

}
