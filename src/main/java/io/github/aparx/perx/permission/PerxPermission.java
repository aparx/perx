package io.github.aparx.perx.permission;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.utils.ArrayPath;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Locale;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 01:09
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxPermission {

  public static final char WILDCARD_OPERATOR = '*';

  private final ArrayPath path;
  private final String name;
  private final boolean isWildcard;
  private final PermissionAdapter adapter;

  protected PerxPermission(ArrayPath path, PermissionAdapter adapter) {
    Preconditions.checkNotNull(path, "Path must not be null");
    Preconditions.checkNotNull(adapter, "Adapter must not be null");
    this.path = path;
    this.name = path.join().toLowerCase(Locale.ENGLISH);
    this.adapter = adapter;
    this.isWildcard = path.last().charAt(0) == WILDCARD_OPERATOR;
  }

  public static PerxPermission of(ArrayPath path, PermissionAdapter assigner) {
    return new PerxPermission(path, assigner);
  }

  public static PerxPermission of(String name, PermissionAdapter assigner) {
    return of(ArrayPath.parse(name), assigner);
  }

  public final boolean isWildcard() {
    return isWildcard;
  }

  public final int length() {
    return path.length();
  }

  public ArrayPath getPath() {
    return path;
  }

  public PermissionAdapter getAdapter() {
    return adapter;
  }

  /** Returns the fully qualified name of this permissions */
  public String getName() {
    return name;
  }

  public boolean has(Permissible permissible) {
    Preconditions.checkNotNull(permissible, "Permissible must not be null");
    return adapter.hasPermission(permissible, getName());
  }

  public void apply(Permissible permissible, boolean value) {
    adapter.setPermission(permissible, getName(), value);
  }

  public void unset(Permissible permissible) {
    adapter.unsetPermission(permissible, getName());
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    PerxPermission that = (PerxPermission) object;
    return isWildcard == that.isWildcard && Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, isWildcard);
  }

  @Override
  public String toString() {
    return "PerxPermission{" +
        "path=" + path +
        ", isWildcard=" + isWildcard +
        '}';
  }
}
