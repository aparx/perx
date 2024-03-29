package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.permission.PermissionAdapter;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:49
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxGroupBuilder {

  protected final PerxGroup group;

  private PerxGroupBuilder(PerxGroup group) {
    Preconditions.checkNotNull(group, "Group must not be null");
    this.group = group;
  }

  public static PerxGroupBuilder delegate(PerxGroup modifiableGroup) {
    return new PerxGroupBuilder(modifiableGroup);
  }

  public static PerxGroupBuilder builder(String name, PermissionAdapter adapter) {
    return delegate(PerxGroup.ofAdapter(name, adapter));
  }

  public static PerxGroupBuilder builder(String name) {
    return delegate(PerxGroup.of(name));
  }

  public static PerxGroupBuilder copyOf(PerxGroupBuilder builder) {
    return delegate(PerxGroup.copyOf(builder.group));
  }

  public static PerxGroupBuilder copyOf(PerxGroup group) {
    return delegate(PerxGroup.copyOf(group));
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder style(GroupStyleKey key, @Nullable String value) {
    this.group.setStyle(key, value);
    return this;
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder setDefault(boolean isDefault) {
    this.group.setDefault(isDefault);
    return this;
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder priority(int priority) {
    this.group.setPriority(priority);
    return this;
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder prefix(@Nullable String prefix) {
    return style(GroupStyleKey.PREFIX, prefix);
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder suffix(@Nullable String suffix) {
    return style(GroupStyleKey.SUFFIX, suffix);
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder addPermission(String permission) {
    return setPermission(permission, true);
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder setPermission(String permission, boolean value) {
    group.getPermissionRepository().set(permission, value);
    return this;
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder addPermissions(String... permissions) {
    if (ArrayUtils.isEmpty(permissions))
      return this;
    HashMap<String, Boolean> map = new HashMap<>();
    for (String key : permissions)
      map.put(key, true);
    return addPermissions(map);
  }

  @CanIgnoreReturnValue
  public PerxGroupBuilder addPermissions(Map<String, Boolean> permissions) {
    if (permissions.isEmpty()) return this;
    group.getPermissionRepository().setAll(permissions);
    return this;
  }

  @CheckReturnValue
  public PerxGroup build() {
    return PerxGroup.copyOf(group);
  }
}
