package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:23
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public enum MessageKey {

  PREFIX("prefix.default", "prefix"),
  SUCCESS_PREFIX("prefix.success"),
  ERROR_PREFIX("prefix.error"),
  ;

  private static final ImmutableMap<ArrayPath, MessageKey> byPath;
  private static final ImmutableMap<String, MessageKey> byReference;

  static {
    MessageKey[] values = values();
    ImmutableMap.Builder<ArrayPath, MessageKey> byPathBuilder =
        ImmutableMap.builderWithExpectedSize(values.length);
    ImmutableMap.Builder<String, MessageKey> byRefBuilder =
        ImmutableMap.builderWithExpectedSize(values.length);
    for (MessageKey key : values) {
      byPathBuilder.put(key.getPath(), key);
      byRefBuilder.put(key.getReference(), key);
    }
    byPath = byPathBuilder.build();
    byReference = byRefBuilder.build();
  }

  private final ArrayPath path;
  private final String reference;

  MessageKey(String path) {
    this(path, path);
  }

  MessageKey(String path, String reference) {
    this(ArrayPath.parse(path), reference);
  }

  MessageKey(ArrayPath path, String reference) {
    Preconditions.checkNotNull(path);
    this.path = path;
    this.reference = reference;
  }

  public static @Nullable MessageKey getByPath(ArrayPath path) {
    return byPath.get(path);
  }

  public static @Nullable MessageKey getByReference(String reference) {
    return byReference.get(reference);
  }

  public LocalizedMessage get(MessageRegister register) {
    return register.get(getPath());
  }

  public LocalizedMessage get() {
    return get(Perx.getInstance().getMessages());
  }

  @CanIgnoreReturnValue
  public @Nullable LocalizedMessage set(MessageRegister register, LocalizedMessage message) {
    return register.set(getPath(), message);
  }

  public ArrayPath getPath() {
    return path;
  }

  public String getReference() {
    return reference;
  }
}
