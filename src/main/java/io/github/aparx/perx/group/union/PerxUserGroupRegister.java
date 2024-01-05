package io.github.aparx.perx.group.union;

import io.github.aparx.perx.group.PerxGroup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 00:25
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxUserGroupRegister {

  @Nullable PerxUserGroup getById(long id);

  @Nullable PerxUserGroup getByGroup(String groupName);

  @Nullable PerxUserGroup getByGroup(PerxGroup group);

  boolean containsId(long id);

  boolean containsGroup(String groupName);

  boolean containsGroup(PerxGroup group);

}
