package io.github.aparx.perx.group.union.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.controller.PerxGroupController;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.BukkitThreads;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 06:34
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserGroupManager implements PerxUserGroupController {

  private static final Function<Collection<PerxUserGroup>, CompletableFuture<List<PerxGroup>>>
      USER_GROUP_TO_GROUP_CONVERTER = (collection) -> CompletableFuture.completedFuture(
      collection.stream()
          .map(PerxUserGroup::findGroup)
          .filter(Objects::nonNull)
          .collect(Collectors.toList()));

  private @Nullable UserGroupDao dao;

  private final Database database;

  private final ListMultimap<UUID, PerxUserGroup> byUser =
      Multimaps.newListMultimap(new ConcurrentHashMap<>(), ArrayList::new);

  private final ListMultimap<String, PerxUserGroup> byGroup =
      Multimaps.newListMultimap(new ConcurrentHashMap<>(), ArrayList::new);

  private final Map<Long, PerxUserGroup> byId = new ConcurrentHashMap<>();

  public PerxUserGroupManager(Database database) {
    this.database = database;
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
    if (byUser.containsKey(userId))
      return CompletableFuture.completedFuture(byUser.get(userId));
    return getDao().getUserGroupsByUser(database, userId)
        .thenApply((models) -> models.stream()
            .map((model) -> {
              @Nullable PerxGroup group =
                  Perx.getInstance().getGroupController().get(model.getGroup().getId());
              return (group != null ? PerxUserGroup.of(model, group) : null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList()))
        .thenApply((list) -> {
          list.forEach(this::register);
          return list;
        });
  }

  public CompletableFuture<List<PerxGroup>> getGroupsByUser(UUID userId) {
    if (byUser.containsKey(userId))
      return USER_GROUP_TO_GROUP_CONVERTER.apply(byUser.get(userId));
    synchronized (this) {
      if (byUser.containsKey(userId))
        return USER_GROUP_TO_GROUP_CONVERTER.apply(byUser.get(userId));
      return getUserGroupsByUser(userId).thenCompose(USER_GROUP_TO_GROUP_CONVERTER);
    }
  }

  public CompletableFuture<List<PerxGroup>> getGroupsByUser(PerxUser user) {
    return getGroupsByUser(user.getId());
  }

  /** Fetches group models by {@code userId} (this is not accessing or modifying the cache) */
  public CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(UUID userId) {
    return getDao().getGroupsByUser(database, userId,
        Perx.getInstance().getGroupController().getDao());
  }

  /** Fetches group models by {@code user} (this is not accessing or modifying the cache) */
  public CompletableFuture<List<GroupModel>> fetchGroupModelsByUser(PerxUser user) {
    return fetchGroupModelsByUser(user.getId()); // TODO potentially cache in `user`?
  }

  @Override
  public CompletableFuture<Boolean> deleteByGroup(String groupName) {
    final String groupId = PerxGroup.formatName(groupName);
    return getDao().deleteByGroup(database, groupName).thenApply((res) -> {
      if (res) removeByGroup(groupId);
      return res;
    });
  }

  @Override
  public CompletableFuture<Boolean> deleteByGroup(PerxGroup group) {
    return deleteByGroup(group.getName());
  }

  public CompletableFuture<Boolean> deleteByUser(UUID userId) {
    return getDao().deleteByUser(database, userId).thenApply((res) -> {
      if (res) removeByUser(userId);
      return res;
    });
  }

  public CompletableFuture<Boolean> deleteByUser(PerxUser user) {
    return deleteByUser(user.getId());
  }

  @Override
  public CompletableFuture<Boolean> deleteById(long id) {
    return database.executeAsync(() -> getDao().deleteById(id)).thenApply((x) -> {
      if (x < 1) return false;
      removeById(id);
      return true;
    });
  }

  @Override
  public Optional<PerxUserGroup> find(UUID userId, String groupName) {
    return byUser.get(userId).stream().filter((x) -> userId.equals(x.getUserId())).findFirst();
  }

  @CanIgnoreReturnValue
  public boolean register(PerxUserGroup userGroup) {
    @Nullable PerxGroup group = userGroup.findGroup();
    if (group == null) return false;
    BukkitThreads.runOnPrimaryThread(() -> {
      byUser.put(userGroup.getUserId(), userGroup);
      byId.put(userGroup.getId(), userGroup);
      byGroup.put(group.getName(), userGroup);
    });
    return true;
  }

  @Override
  public void removeByUser(UUID userId) {
    BukkitThreads.runOnPrimaryThread(() -> {
      byUser.get(userId).forEach((userGroup) -> {
        @Nullable PerxGroup group = userGroup.findGroup();
        if (group != null)
          byGroup.remove(group.getName(), userGroup);
        byId.remove(userGroup.getId());
      });
      byUser.removeAll(userId);
    });
  }

  @Override
  public void removeByGroup(String groupName) {
    final String group = PerxGroup.formatName(groupName);
    BukkitThreads.runOnPrimaryThread(() -> {
      byGroup.get(group).forEach((userGroup) -> {
        userGroup.markRemoved();
        byUser.remove(userGroup.getUserId(), userGroup);
        byId.remove(userGroup.getId());
      });
      byGroup.removeAll(group);
    });
  }

  @Override
  public void removeById(long id) {
    BukkitThreads.runOnPrimaryThread(() -> {
      @Nullable PerxUserGroup userGroup = byId.get(id);
      if (userGroup == null) return;
      byUser.remove(userGroup.getUserId(), userGroup);
      @Nullable PerxGroup group = userGroup.findGroup();
      if (group != null) byGroup.remove(group.getName(), userGroup);
      byId.remove(id);
    });
  }

  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().createOrUpdate(userGroupModel));
  }

  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxUserGroup userGroup) {
    return upsert(userGroup.toModel());
  }

  public CompletableFuture<Integer> update(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().update(userGroupModel));
  }

  public CompletableFuture<Integer> update(PerxUserGroup userGroup) {
    return update(userGroup.toModel());
  }

  public CompletableFuture<Integer> create(UserGroupModel userGroupModel) {
    return database.executeAsync(() -> getDao().create(userGroupModel));
  }

  public CompletableFuture<Integer> create(PerxUserGroup userGroup) {
    return create(userGroup.toModel());
  }

}
