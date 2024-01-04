package io.github.aparx.perx.permission;

import com.google.common.base.Preconditions;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A PermissionAdapter implementation, that uses Bukkit's plugin attachments to apply permissions.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 01:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class AttachingPermissionAdapter implements PermissionAdapter {

  private final Plugin plugin;

  private final Map<Permissible, PermissionAttachment> attachments = new WeakHashMap<>();

  public AttachingPermissionAdapter(Plugin plugin) {
    Preconditions.checkNotNull(plugin, "Plugin must not be null");
    this.plugin = plugin;
  }

  @Override
  public void clearPermissions(Permissible permissible) {
    @Nullable PermissionAttachment attachment = attachments.remove(permissible);
    if (attachment != null) permissible.removeAttachment(attachment);
  }

  @Override
  public void setPermission(Permissible permissible, String name, boolean value) {
    // TODO test rooted wildcard
    getOrCreateAttachment(permissible).setPermission(name, value);
  }

  @Override
  public void unsetPermission(Permissible permissible, String name) {

    getOrCreateAttachment(permissible).unsetPermission(name);
  }

  @Override
  public boolean hasPermission(Permissible permissible, String name) {
    return permissible.hasPermission(name);
  }

  public boolean isWildcardRoot(String name) {
    return String.valueOf(PerxPermission.WILDCARD_OPERATOR).equals(name);
  }

  protected final PermissionAttachment getOrCreateAttachment(Permissible permissible) {
    return attachments.computeIfAbsent(permissible, (key) -> key.addAttachment(plugin));
  }
}
