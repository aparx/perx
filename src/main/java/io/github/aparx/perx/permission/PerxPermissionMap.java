package io.github.aparx.perx.permission;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 01:28
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxPermissionMap implements PerxPermissionRegister {

  private final Map<ArrayPath, PerxPermission> map = new HashMap<>();

  private final PermissionAdapter adapter;

  public PerxPermissionMap(PermissionAdapter adapter) {
    Preconditions.checkNotNull(adapter, "Adapter must not be null");
    this.adapter = adapter;
  }

  @Override
  public PerxPermissionRegister copy() {
    PerxPermissionMap permissions = new PerxPermissionMap(adapter);
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
  public @Nullable PerxPermission get(@NonNull ArrayPath path) {
    return map.get(path);
  }

  @Override
  public @Nullable PerxPermission get(@NonNull String name) {
    return get(ArrayPath.parse(name));
  }

  @Override
  @CanIgnoreReturnValue
  public boolean register(@NonNull PerxPermission permission) {
    return map.putIfAbsent(permission.getPath(), permission) == null;
  }

  @Override
  @CanIgnoreReturnValue
  public PerxPermission set(@NonNull ArrayPath path, boolean value) {
    PerxPermission permission = map.computeIfAbsent(path,
        (key) -> PerxPermission.of(path, getAdapter(), value));
    permission.setValue(value);
    return permission;
  }

  @Override
  @CanIgnoreReturnValue
  public PerxPermission set(@NonNull String name, boolean value) {
    return set(ArrayPath.parse(name.toLowerCase(Locale.ENGLISH)), value);
  }

  @Override
  public void setAll(@NonNull Map<String, Boolean> map) {
    for (Map.Entry<String, Boolean> entry : map.entrySet())
      set(entry.getKey(), entry.getValue());
  }

  @Override
  @CanIgnoreReturnValue
  public boolean remove(@NonNull PerxPermission permission) {
    return map.remove(permission.getPath(), permission);
  }

  @Override
  public @Nullable PerxPermission remove(@NonNull ArrayPath path) {
    return map.remove(path);
  }

  @Override
  public @Nullable PerxPermission remove(@NonNull String name) {
    return map.remove(ArrayPath.parse(name));
  }

  @Override
  public boolean contains(@NonNull String name) {
    return map.containsKey(ArrayPath.parse(name));
  }

  @Override
  public boolean contains(@NonNull PerxPermission permission) {
    return permission.equals(map.get(permission.getPath()));
  }

  @Override
  public Map<String, Boolean> toPermissionMap() {
    Map<String, Boolean> newMap = new HashMap<>(map.size());
    for (Map.Entry<ArrayPath, PerxPermission> entry : map.entrySet())
      newMap.put(entry.getKey().join(), entry.getValue().getValue());
    return newMap;
  }

  @Override
  public Collection<PerxPermission> toCollection() {
    return new ArrayList<>(map.values());
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
