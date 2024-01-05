package io.github.aparx.perx.group.many;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.database.PerxModelController;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupDao;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.user.PerxUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 23:52
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxUserGroupController extends PerxModelController<UserGroupDao> {

  /** Returns the already cached groups for given user, or fetches & caches them */
  CompletableFuture<List<PerxUserGroup>> getUserGroupsByUser(UUID userId);

  /** Returns the already cached groups for given user, or fetches & caches them */
  CompletableFuture<List<PerxGroup>> getGroupsByUser(UUID userId);

  CompletableFuture<List<PerxGroup>> getGroupsByUser(PerxUser user);

  CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(UUID userId);

  CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(PerxUser user);

  CompletableFuture<Boolean> deleteByGroup(String groupName);

  CompletableFuture<Boolean> deleteByGroup(PerxGroup group);

  CompletableFuture<Boolean> deleteByUser(UUID userId);

  CompletableFuture<Boolean> deleteByUser(PerxUser user);

  CompletableFuture<Boolean> deleteById(long id);

  Optional<PerxUserGroup> find(UUID userId, String groupName);

  /**
   * Registers and potentially overrides any already similarly registered group in the cache.
   * <p>This method only accesses the cache. Thus, it has no effect on long term storage.
   *
   * @param userGroup the user group to be introduced to the cache
   * @return true if the user group could be registered
   */
  @CanIgnoreReturnValue
  boolean register(PerxUserGroup userGroup);

  /** Removes all user groups associated with given user (just in cache) */
  void removeByUser(UUID userId);

  /** Removes all user groups associated with given group (just in cache) */
  void removeByGroup(String groupName);

  /** Removes the user group associated to given ID (just in cache) */
  @CanIgnoreReturnValue
  boolean removeById(long id);

}
