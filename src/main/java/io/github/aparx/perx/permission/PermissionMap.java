package io.github.aparx.perx.permission;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 01:28
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PermissionMap implements PermissionRegister {

  private final Map<String, PerxPermission> map = new HashMap<>();

  private final PermissionAdapter adapter;

  public PermissionMap(PermissionAdapter adapter) {
    Preconditions.checkNotNull(adapter, "Adapter must not be null");
    this.adapter = adapter;
  }

  @Override
  public PermissionRegister copy() {
    PermissionMap permissions = new PermissionMap(adapter);
    permissions.map.putAll(map);
    return permissions;
  }

  @Override
  public PermissionAdapter getAdapter() {
    return adapter;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public @Nullable PerxPermission get(@NonNull String name) {
    return map.get(name.toLowerCase(Locale.ENGLISH));
  }

  @Override
  @CanIgnoreReturnValue
  public boolean register(@NonNull PerxPermission permission) {
    return map.putIfAbsent(permission.getName(), permission) == null;
  }

  @Override
  @CanIgnoreReturnValue
  public PerxPermission register(@NonNull ArrayPath path) {
    return map.computeIfAbsent(path.join(), (key) -> PerxPermission.of(path, getAdapter()));
  }

  @Override
  @CanIgnoreReturnValue
  public PerxPermission register(@NonNull String name) {
    return register(ArrayPath.parse(name));
  }

  @Override
  @CanIgnoreReturnValue
  public boolean registerAll(@NonNull Collection<String> names) {
    boolean mod = false;
    for (String name : names)
      mod |= register(PerxPermission.of(ArrayPath.parse(name), getAdapter()));
    return mod;
  }

  @Override
  @CanIgnoreReturnValue
  public boolean remove(@NonNull PerxPermission permission) {
    return map.remove(permission.getName(), permission);
  }

  @Override
  public boolean contains(@NonNull String name) {
    return map.containsKey(name);
  }

  @Override
  public boolean contains(@NonNull PerxPermission permission) {
    return permission.equals(map.get(permission.getName()));
  }

  @Override
  public @NonNull Iterator<PerxPermission> iterator() {
    return map.values().iterator();
  }

  @Override
  public String toString() {
    return "PermissionMap{" +
        "map=" + map +
        '}';
  }
}
