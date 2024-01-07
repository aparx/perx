package io.github.aparx.perx.permission;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import io.github.aparx.perx.utils.Copyable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * A simple map-like data structure that stores {@code PerxPermission} instances.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:59
 * @since 1.0
 */
public interface PerxPermissionRepository extends Iterable<PerxPermission>,
    Copyable<PerxPermissionRepository> {

  /**
   * Returns an interface that handles the giving and revoking of permissions of permissibles.
   *
   * @return the permissions adapter
   */
  PermissionAdapter getAdapter();

  int size();

  @Nullable PerxPermission get(@NonNull ArrayPath path);

  /**
   * Returns the permissions with given fully qualified name.
   *
   * @param name the fully qualified name of the permissions
   * @return the permissions instance
   */
  @Nullable PerxPermission get(@NonNull String name);

  /** Registers {@code permission} to this register, if not already known */
  @CanIgnoreReturnValue
  boolean register(@NonNull PerxPermission permission);

  @CanIgnoreReturnValue
  PerxPermission set(@NonNull ArrayPath path, boolean value);

  @CanIgnoreReturnValue
  PerxPermission set(@NonNull String name, boolean value);

  @CanIgnoreReturnValue
  void setAll(@NonNull Map<String, Boolean> map);

  @CanIgnoreReturnValue
  boolean remove(@NonNull PerxPermission permission);

  @CanIgnoreReturnValue
  @Nullable PerxPermission remove(@NonNull ArrayPath path);

  @CanIgnoreReturnValue
  @Nullable PerxPermission remove(@NonNull String name);

  boolean contains(@NonNull String name);

  boolean contains(@NonNull PerxPermission permission);

  /** Returns a new map that contains all permissions mapped to their respective value */
  Map<String, Boolean> toPermissionMap();

  Collection<PerxPermission> toCollection();

}
