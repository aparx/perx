package io.github.aparx.perx.database.data.group;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.events.PerxGroupMutateEvent;
import io.github.aparx.perx.events.PerxMutateType;
import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 06:25
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
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
    return publishEvent(PerxMutateType.CREATE, group).thenCompose((__) -> {
      return database.executeAsync(() -> create(group.toModel())).thenApply((x) -> x > 0);
    });
  }

  public CompletableFuture<CreateOrUpdateStatus> upsert(Database database, PerxGroup group) {
    return publishEvent(PerxMutateType.UPSERT, group).thenCompose((__) -> {
      return database.executeAsync(() -> createOrUpdate(group.toModel()));
    });
  }

  public CompletableFuture<Integer> update(Database database, PerxGroup group) {
    return publishEvent(PerxMutateType.UPDATE, group).thenCompose((__) -> {
      return database.executeAsync(() -> update(group.toModel()));
    });
  }

  public CompletableFuture<Boolean> delete(Database database, String name) {
    final String finalName = PerxGroup.transformKey(name);
    @Nullable PerxGroup group = Perx.getInstance().getGroupService().getRepository().get(name);
    Function<? super Void, CompletableFuture<Boolean>> composer = (__) ->
        database.executeAsync(() -> deleteById(finalName)).thenApply((x) -> x > 0);
    if (group != null)
      return publishEvent(PerxMutateType.DELETE, group).thenCompose(composer);
    return composer.apply(null);
  }

  private CompletableFuture<Void> publishEvent(PerxMutateType type, PerxGroup group) {
    PerxGroupMutateEvent event = new PerxGroupMutateEvent(type, group);
    Bukkit.getPluginManager().callEvent(event);
    if (event.isCancelled())
      return CompletableFuture.failedFuture(new RuntimeException("Action cancelled"));
    return CompletableFuture.completedFuture(null);
  }

}
