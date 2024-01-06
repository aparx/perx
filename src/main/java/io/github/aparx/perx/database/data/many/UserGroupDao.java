package io.github.aparx.perx.database.data.many;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 05:57
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class UserGroupDao extends BaseDaoImpl<UserGroupModel, Long> {

  public UserGroupDao() throws SQLException {
    super(UserGroupModel.class);
  }

  public UserGroupDao(ConnectionSource connectionSource, Class<UserGroupModel> dataClass) throws SQLException {
    super(connectionSource, dataClass);
  }

  public UserGroupDao(
      ConnectionSource connectionSource,
      DatabaseTableConfig<UserGroupModel> tableConfig)
      throws SQLException {
    super(connectionSource, tableConfig);
  }

  public CompletableFuture<List<UserGroupModel>> getUserGroupsByUser(
      Database database, UUID userId) {
    return database.executeAsync(() -> query(createUserGroupsByUserQuery(userId)));
  }

  public CompletableFuture<List<GroupModel>> getGroupsByUser(
      Database database, UUID userId, Dao<GroupModel, String> dao) {
    return database.executeAsync(() -> dao.query(createGroupByUserQuery(userId, dao)));
  }

  public CompletableFuture<Set<UUID>> getUsersByGroup(Database database, String groupName) {
    return database.executeAsync(() -> {
      // we get all users and map them to a set (test performance?)
      return query(createUsersByGroupQuery(groupName)).stream()
          .map(UserGroupModel::getUserId)
          .collect(Collectors.toSet());
    });
  }

  public CompletableFuture<Boolean> deleteByGroup(Database database, String groupName) {
    return database.executeAsync(() -> {
      DeleteBuilder<UserGroupModel, Long> deleteBuilder = deleteBuilder();
      deleteBuilder.where().eq(UserGroupModel.GROUP_ID_FIELD_NAME, groupName);
      return deleteBuilder.delete();
    }).thenApply((x) -> x > 0);
  }

  public CompletableFuture<Boolean> deleteByUser(Database database, UUID userId) {
    return database.executeAsync(() -> {
      DeleteBuilder<UserGroupModel, Long> deleteBuilder = deleteBuilder();
      deleteBuilder.where().eq(UserGroupModel.USER_ID_FIELD_NAME, userId);
      return deleteBuilder.delete();
    }).thenApply((x) -> x > 0);
  }

  protected PreparedQuery<UserGroupModel> createUserGroupsByUserQuery(UUID userId) throws SQLException {
    QueryBuilder<UserGroupModel, Long> userGroupQb = queryBuilder();
    userGroupQb.where().eq(UserGroupModel.USER_ID_FIELD_NAME, userId);
    return userGroupQb.prepare();
  }

  protected PreparedQuery<GroupModel> createGroupByUserQuery(
      UUID userId, Dao<GroupModel, String> groupDao) throws SQLException {
    QueryBuilder<UserGroupModel, Long> userGroupQb = queryBuilder();
    userGroupQb.selectColumns(UserGroupModel.GROUP_ID_FIELD_NAME);
    userGroupQb.where().eq(UserGroupModel.USER_ID_FIELD_NAME, userId);
    QueryBuilder<GroupModel, String> groupQb = groupDao.queryBuilder();
    groupQb.where().in("id", userGroupQb);
    return groupQb.prepare();
  }

  protected PreparedQuery<UserGroupModel> createUsersByGroupQuery(String groupName) throws SQLException {
    QueryBuilder<UserGroupModel, Long> userGroupQb = queryBuilder();
    userGroupQb.selectColumns(UserGroupModel.USER_ID_FIELD_NAME);
    userGroupQb.where().eq(UserGroupModel.GROUP_ID_FIELD_NAME, groupName);
    return userGroupQb.prepare();
  }
}
