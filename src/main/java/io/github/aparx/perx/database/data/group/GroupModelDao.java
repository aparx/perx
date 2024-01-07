package io.github.aparx.perx.database.data.group;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.group.PerxGroup;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 06:25
 * @since 1.0
 */
public class GroupModelDao extends BaseDaoImpl<GroupModel, String> {

  public GroupModelDao() throws SQLException {
    super(GroupModel.class);
  }

  public GroupModelDao(
      ConnectionSource connectionSource,
      Class<GroupModel> dataClass)
      throws SQLException {
    super(connectionSource, dataClass);
  }

  public GroupModelDao(
      ConnectionSource connectionSource,
      DatabaseTableConfig<GroupModel> tableConfig)
      throws SQLException {
    super(connectionSource, tableConfig);
  }

  public CompletableFuture<Boolean> create(Database database, PerxGroup group) {
    return database.executeAsync(() -> create(group.toModel())).thenApply((x) -> x > 0);
  }

  public CompletableFuture<CreateOrUpdateStatus> upsert(Database database, PerxGroup group) {
    return database.executeAsync(() -> createOrUpdate(group.toModel()));
  }

  public CompletableFuture<Integer> update(Database database, PerxGroup group) {
    return database.executeAsync(() -> update(group.toModel()));
  }

  public CompletableFuture<Boolean> delete(Database database, String name) {
    final String finalName = PerxGroup.transformKey(name);
    return database.executeAsync(() -> deleteById(finalName)).thenApply((x) -> x > 0);
  }

}
