package io.github.aparx.perx.permission;

import io.github.aparx.perx.group.PerxGroup;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-09 14:00
 * @since 1.0
 */
@FunctionalInterface
public interface PermissionAdapterFactory {

  PermissionAdapter createAdapter(PerxGroup group);

}
