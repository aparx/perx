package io.github.aparx.perx.message;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.function.BiConsumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:26
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface MessageRegister {

  /**
   * Returns the message at given path.
   *
   * @param path the path to retrieve the message from
   * @return the message for given path
   */
  LocalizedMessage get(ArrayPath path);

  /**
   * Returns the message at given path.
   *
   * @param path the path to retrieve the message from
   * @return the message for given path
   */
  LocalizedMessage get(String path);

  /**
   * Sets the message at given path and overwrites any previous message at {@code path}.
   *
   * @param path    the path to associate {@code message} to
   * @param message the message to be associated to {@code path}
   * @return the potentially previously, now overwritten, message at given path (optional)
   */
  @CanIgnoreReturnValue
  @Nullable LocalizedMessage set(ArrayPath path, LocalizedMessage message);

  boolean contains(ArrayPath path);

  void forEach(BiConsumer<ArrayPath, LocalizedMessage> action);

}
