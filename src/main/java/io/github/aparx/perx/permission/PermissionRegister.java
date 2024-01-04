package io.github.aparx.perx.permission;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import io.github.aparx.perx.utils.Copyable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * A simple map-like data structure that stores {@code PerxPermission} instances.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:59
 * @since 1.0
 */
public interface PermissionRegister extends Iterable<PerxPermission>, Copyable<PermissionRegister> {

  /**
   * Returns an interface that handles the giving and revoking of permissions of permissibles.
   *
   * @return the permission adapter
   */
  PermissionAdapter getAdapter();

  int size();

  /**
   * Returns the permission with given fully qualified name.
   *
   * @param name the fully qualified name of the permission
   * @return the permission instance
   */
  @Nullable PerxPermission get(@NonNull String name);

  @CanIgnoreReturnValue
  boolean register(@NonNull PerxPermission permission);

  @CanIgnoreReturnValue
  PerxPermission register(@NonNull ArrayPath path);

  @CanIgnoreReturnValue
  PerxPermission register(@NonNull String name);

  @CanIgnoreReturnValue
  boolean registerAll(@NonNull Collection<String> name);

  @CanIgnoreReturnValue
  boolean remove(@NonNull PerxPermission permission);

  boolean contains(@NonNull String name);

  boolean contains(@NonNull PerxPermission permission);

}
