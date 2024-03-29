package io.github.aparx.perx.group;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.database.PerxModelService;
import io.github.aparx.perx.database.data.group.GroupModelDao;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A simple map-like data structure that stores {@code PerxGroup} instances.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:29
 * @since 1.0
 */
@CanIgnoreReturnValue
@DefaultQualifier(NonNull.class)
public interface PerxGroupService extends PerxModelService<GroupModelDao> {

  @CheckReturnValue
  PerxGroupRepository getRepository();

  @CheckReturnValue
  Collection<PerxGroup> getDefaults();

  /**
   * Creates and registers {@code group} to this local controller and the database.
   *
   * @param group the group to create and register
   * @return the resolving future, with true if the group was created
   */
  CompletableFuture<Boolean> create(PerxGroup group);

  /**
   * Updates {@code group} or inserts it into this controller and the database.
   *
   * @param group the group to upsert
   * @return the resolving future, with the amount of updated rows
   */
  CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group);

  CompletableFuture<Integer> update(PerxGroup group);

  CompletableFuture<Boolean> delete(String name);

}
