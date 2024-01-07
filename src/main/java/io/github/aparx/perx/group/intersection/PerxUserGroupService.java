package io.github.aparx.perx.group.intersection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.database.PerxModelService;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupDao;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.user.PerxUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 23:52
 * @since 1.0
 */
@CanIgnoreReturnValue
@DefaultQualifier(NonNull.class)
public interface PerxUserGroupService extends PerxModelService<UserGroupDao> {

  @CheckReturnValue
  PerxUserGroupRepository getRepository();

  /** Returns the already cached groups for given user, or fetches & caches them */
  @CheckReturnValue
  CompletableFuture<List<PerxUserGroup>> getUserGroupsByUser(UUID userId);

  /** Returns the already cached groups for given user, or fetches & caches them */
  @CheckReturnValue
  CompletableFuture<List<PerxGroup>> getGroupsByUser(UUID userId);

  @CheckReturnValue
  CompletableFuture<List<PerxGroup>> getGroupsByUser(PerxUser user);

  @CheckReturnValue
  CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(UUID userId);

  @CheckReturnValue
  CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(PerxUser user);

  CompletableFuture<Boolean> deleteByGroup(String groupName);

  CompletableFuture<Boolean> deleteByGroup(PerxGroup group);

  CompletableFuture<Boolean> deleteByUser(UUID userId);

  CompletableFuture<Boolean> deleteByUser(PerxUser user);

  CompletableFuture<Boolean> deleteById(long id);

  CompletableFuture<Dao.CreateOrUpdateStatus> upsert(UserGroupModel userGroupModel);

  CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxUserGroup userGroup);

  CompletableFuture<Integer> update(UserGroupModel userGroupModel);

  CompletableFuture<Integer> update(PerxUserGroup userGroup);

  CompletableFuture<Integer> create(UserGroupModel userGroupModel);

  CompletableFuture<Integer> create(PerxUserGroup userGroup);

}
