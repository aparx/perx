package io.github.aparx.perx.database.data.many;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
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
public class UserGroupDAO extends BaseDaoImpl<UserGroup, Integer> {

  public UserGroupDAO() throws SQLException {
    super(UserGroup.class);
  }

  public UserGroupDAO(ConnectionSource connectionSource, Class<UserGroup> dataClass) throws SQLException {
    super(connectionSource, dataClass);
  }

  public UserGroupDAO(
      ConnectionSource connectionSource,
      DatabaseTableConfig<UserGroup> tableConfig)
      throws SQLException {
    super(connectionSource, tableConfig);
  }

  public CompletableFuture<List<GroupModel>> getGroupsByUser(
      Database database, UUID userId, Dao<GroupModel, String> dao) {
    return database.executeAsync(() -> dao.query(createGroupByUserQuery(userId, dao)));
  }

  public CompletableFuture<Set<UUID>> getUsersByGroup(Database database, String groupId) {
    return database.executeAsync(() -> {
      // we get all users and map them to a set (test performance?)
      return query(createUsersByGroupQuery(groupId)).stream()
          .map(UserGroup::getUser)
          .collect(Collectors.toSet());
    });
  }

  public CompletableFuture<Boolean> deleteByUser(Database database, UUID userId) {
    return database.executeAsync(() -> {
      return deleteBuilder().where().eq(UserGroup.USER_ID_FIELD_NAME, userId).query();
    }).thenApply((x) -> !x.isEmpty());
  }

  protected PreparedQuery<GroupModel> createGroupByUserQuery(
      UUID userId, Dao<GroupModel, String> groupDao) throws SQLException {
    QueryBuilder<UserGroup, Integer> userGroupQb = queryBuilder();
    userGroupQb.selectColumns(UserGroup.GROUP_ID_FIELD_NAME);
    userGroupQb.where().eq(UserGroup.USER_ID_FIELD_NAME, userId);
    QueryBuilder<GroupModel, String> groupQb = groupDao.queryBuilder();
    groupQb.where().in("id", userGroupQb);
    return groupQb.prepare();
  }

  protected PreparedQuery<UserGroup> createUsersByGroupQuery(String groupId) throws SQLException {
    QueryBuilder<UserGroup, Integer> userGroupQb = queryBuilder();
    userGroupQb.selectColumns(UserGroup.USER_ID_FIELD_NAME);
    userGroupQb.where().eq(UserGroup.GROUP_ID_FIELD_NAME, groupId);
    return userGroupQb.prepare();
  }
}
