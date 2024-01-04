package io.github.aparx.perx.database.data.many;

import com.google.common.base.Preconditions;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.PerxModelController;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupController;
import io.github.aparx.perx.user.PerxUser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 06:34
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class UserGroupController implements PerxModelController<UserGroupDAO> {

  private @Nullable UserGroupDAO dao;

  private final Database database;
  private final PerxGroupController groupController;

  public UserGroupController(Database database, PerxGroupController groupController) {
    this.database = database;
    this.groupController = groupController;
  }

  public Database getDatabase() {
    return database;
  }

  public PerxGroupController getGroupController() {
    return groupController;
  }

  @Override
  public void load() {
    database.queue((db) -> {
      ConnectionSource dbSource = db.getSourceLoudly();
      this.dao = DaoManager.createDao(dbSource, UserGroup.class);
      db.executeAsync(() -> TableUtils.createTableIfNotExists(dbSource, UserGroup.class));
    });
  }

  @Override
  public UserGroupDAO getDao() {
    @Nullable UserGroupDAO dao = this.dao;
    Preconditions.checkArgument(dao != null, "DAO is not initialized");
    return dao;
  }

  public CompletableFuture<List<PerxGroup>> getGroupsByUser(UUID userId) {
    return getGroupModelsByUser(userId).thenApply((models) ->
        models.stream()
            .map(GroupModel::getId)
            .map(groupController::get)
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList()));
  }

  public CompletableFuture<List<PerxGroup>> getGroupsByUser(PerxUser user) {
    return getGroupsByUser(user.getId());
  }

  public CompletableFuture<List<GroupModel>> getGroupModelsByUser(UUID userId) {
    return getDao().getGroupsByUser(database, userId, getGroupController().getDao());
  }

  public CompletableFuture<List<GroupModel>> getGroupModelsByUser(PerxUser user) {
    return getGroupModelsByUser(user.getId()); // TODO potentially cache in `user`?
  }

  public CompletableFuture<Set<UUID>> getUsersByGroup(String groupId) {
    return getDao().getUsersByGroup(database, groupId);
  }

  public CompletableFuture<Set<UUID>> getUsersByGroup(PerxGroup group) {
    return getUsersByGroup(group.getName()); // TODO potentially cache in `group`?
  }

  public CompletableFuture<Boolean> deleteByUser(UUID userId) {
    return getDao().deleteByUser(database, userId);
  }

  public CompletableFuture<Boolean> deleteByUser(PerxUser user) {
    return deleteByUser(user.getId());
  }

}
