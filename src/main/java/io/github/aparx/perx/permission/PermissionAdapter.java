package io.github.aparx.perx.permission;

import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Handling interface that manages the giving and revoking of permissions and their attachments.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 01:20
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PermissionAdapter {

  /**
   * Clears the permissions added through this adapter of {@code permissible}.
   *
   * @param permissible the permissible to clear the permissions of
   */
  void clearPermissions(Permissible permissible);

  void setPermission(Permissible permissible, String name, boolean value);

  void unsetPermission(Permissible permissible, String name);

  boolean hasPermission(Permissible permissible, String name);

}
