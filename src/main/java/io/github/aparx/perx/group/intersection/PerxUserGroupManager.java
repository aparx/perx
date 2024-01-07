package io.github.aparx.perx.group.intersection;

import com.google.common.base.Preconditions;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupDao;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupRepository;
import io.github.aparx.perx.group.PerxGroupService;
import io.github.aparx.perx.user.PerxUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 06:34
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserGroupManager implements PerxUserGroupService {

  private static final Function<Collection<PerxUserGroup>, CompletableFuture<List<PerxGroup>>>
      USER_GROUP_TO_GROUP_CONVERTER = (collection) -> CompletableFuture.completedFuture(
      collection.stream()
          .map(PerxUserGroup::findGroup)
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));

  private @Nullable UserGroupDao dao;

  private final Database database;

  private final PerxUserGroupRepository repository;

  public PerxUserGroupManager(Database database) {
    this(database, new PerxUserGroupCache());
  }

  public PerxUserGroupManager(Database database, PerxUserGroupRepository repository) {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(repository, "Repository must not be null");
    this.repository = repository;
    this.database = database;
  }

  public PerxUserGroupRepository getRepository() {
    return repository;
  }

  public Database getDatabase() {
    return database;
  }

  @Override
  public void load() {
    database.queue((db) -> {
      ConnectionSource dbSource = db.getSourceLoudly();
      this.dao = DaoManager.createDao(dbSource, UserGroupModel.class);
      db.executeAsync(() -> TableUtils.createTableIfNotExists(dbSource, UserGroupModel.class));
    });
  }

  @Override
  public UserGroupDao getDao() {
    @Nullable UserGroupDao dao = this.dao;
    Preconditions.checkArgument(dao != null, "DAO is not initialized");
    return dao;
  }

  @Override
  public CompletableFuture<List<PerxUserGroup>> getUserGroupsByUser(UUID userId) {
    if (repository.hasUser(userId))
      return CompletableFuture.completedFuture(repository.findByUser(userId));
    return getDao().getUserGroupsByUser(database, userId)
        .thenApply((models) -> models.stream()
            .map((model) -> {
              PerxGroupService groupService = Perx.getInstance().getGroupService();
              PerxGroupRepository groupRepository = groupService.getRepository();
              @Nullable PerxGroup group = groupRepository.get(model.getGroup().getId());
              return (group != null ? PerxUserGroup.of(model, group) : null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList()))
        .thenApply((list) -> {
          list.forEach(repository::put);
          return list;
        });
  }

  @Override
  public CompletableFuture<List<PerxGroup>> getGroupsByUser(UUID userId) {
    return getUserGroupsByUser(userId).thenCompose(USER_GROUP_TO_GROUP_CONVERTER);
  }

  @Override
  public CompletableFuture<List<PerxGroup>> getGroupsByUser(PerxUser user) {
    return getGroupsByUser(user.getId());
  }

  /** Fetches group models by {@code userId} (this is not accessing or modifying the cache) */
  @Override
  public CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(UUID userId) {
    return getDao().getGroupsByUser(database, userId,
        Perx.getInstance().getGroupService().getDao());
  }

  /** Fetches group models by {@code user} (this is not accessing or modifying the cache) */
  @Override
  public CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(PerxUser user) {
    return fetchGroupModelsByUser(user.getId()); // TODO potentially cache in `user`?
  }

  @Override
  public CompletableFuture<Boolean> deleteByGroup(String groupName) {
    final String groupId = PerxGroup.transformKey(groupName);
    return getDao().deleteByGroup(database, groupName).thenApply((res) -> {
      if (res) repository.removeByGroup(groupId);
      return res;
    });
  }

  @Override
  public CompletableFuture<Boolean> deleteByGroup(PerxGroup group) {
    return deleteByGroup(group.getName());
  }

  @Override
  public CompletableFuture<Boolean> deleteByUser(UUID userId) {
    return getDao().deleteByUser(database, userId).thenApply((res) -> {
      if (res) repository.removeByUser(userId);
      return res;
    });
  }

  @Override
  public CompletableFuture<Boolean> deleteByUser(PerxUser user) {
    return deleteByUser(user.getId());
  }

  @Override
  public CompletableFuture<Boolean> deleteById(long id) {
    return database.executeAsync(() -> getDao().deleteById(id)).thenApply((x) -> {
      if (x < 1) return false;
      repository.removeById(id);
      return true;
    });
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().createOrUpdate(userGroupModel));
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxUserGroup userGroup) {
    return upsert(userGroup.toModel());
  }

  @Override
  public CompletableFuture<Integer> update(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().update(userGroupModel));
  }

  @Override
  public CompletableFuture<Integer> update(PerxUserGroup userGroup) {
    return update(userGroup.toModel());
  }

  @Override
  public CompletableFuture<Integer> create(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().create(userGroupModel));
  }

  @Override
  public CompletableFuture<Integer> create(PerxUserGroup userGroup) {
    return create(userGroup.toModel());
  }

}
