package io.github.aparx.perx.group.controller;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.database.PerxModelController;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.utils.Copyable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A simple map-like data structure that stores {@code PerxGroup} instances.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:29
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxGroupController extends Iterable<PerxGroup>, Copyable<PerxGroupController>,
    PerxModelController<Dao<GroupModel, String>> {

  Dao<GroupModel, String> getDao();

  /**
   * Creates and registers {@code group} to this local controller and the database.
   *
   * @param group the group to create and register
   * @return the resolving future, with true if the group was created
   */
  @CanIgnoreReturnValue
  CompletableFuture<Boolean> create(PerxGroup group);

  /**
   * Updates {@code group} or inserts it into this controller and the database.
   *
   * @param group the group to upsert
   * @return the resolving future, with the amount of updated rows
   */
  @CanIgnoreReturnValue
  CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group);

  @CanIgnoreReturnValue
  CompletableFuture<Boolean> delete(String name);

  int size();

  @CanIgnoreReturnValue
  boolean register(PerxGroup group);

  @CanIgnoreReturnValue
  boolean remove(PerxGroup group);

  @CanIgnoreReturnValue
  @Nullable PerxGroup remove(String name);

  boolean contains(PerxGroup group);

  boolean contains(String name);

  Collection<PerxGroup> getDefaults();

  @Nullable PerxGroup get(String name);

  default PerxGroup getLoudly(String name) {
    @Nullable PerxGroup group = get(name);
    Preconditions.checkArgument(group != null, "Group " + name + " unknown");
    return group;
  }

}
