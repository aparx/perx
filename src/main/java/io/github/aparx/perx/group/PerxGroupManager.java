package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.group.GroupModelDao;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.group.intersection.PerxUserGroupService;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:30
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxGroupManager implements PerxGroupService {

  private @Nullable GroupModelDao dao;

  private final Database database;
  private final PerxGroupRepository repository;

  public PerxGroupManager(Database database) {
    this(database, new PerxGroupCache());
  }

  public PerxGroupManager(Database database, PerxGroupRepository repository) {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(repository, "Repository must not be null");
    this.database = database;
    this.repository = repository;
  }

  @Override
  public void load() {
    database.executeAsync(() -> {
      ConnectionSource dbSource = database.getSourceLoudly();
      this.dao = DaoManager.createDao(dbSource, GroupModel.class);
      TableUtils.createTableIfNotExists(dbSource, GroupModel.class);
      dao.queryForAll().stream()
          .filter((model) -> repository.register(PerxGroup.of(model)))
          .forEach((model) -> Perx.getLogger().log(Level.INFO, "Fetched: {0}", model.getId()));
      Bukkit.getScheduler().runTask(Perx.getPlugin(),
          () -> Bukkit.getPluginManager().callEvent(new GroupsFetchedEvent(this, false)));
    });
  }


  @Override
  public GroupModelDao getDao() {
    @Nullable GroupModelDao dao = this.dao;
    Preconditions.checkArgument(dao != null, "DAO is not initialized");
    return dao;
  }

  @Override
  public PerxGroupRepository getRepository() {
    return repository;
  }

  @Override
  public Collection<PerxGroup> getDefaults() {
    return repository.stream().filter(PerxGroup::isDefault).collect(Collectors.toList());
  }

  @Override
  public CompletableFuture<Boolean> create(PerxGroup group) {
    return getDao().create(database, group).thenApply((result) -> {
      return !result || repository.register(group);
    });
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group) {
    return getDao().upsert(database, group).thenApply((result) -> {
      repository.put(group);
      return result;
    });
  }

  @Override
  public CompletableFuture<Integer> update(PerxGroup group) {
    return getDao().update(database, group).thenApply((rowsUpdated) -> {
      // TODO consider updating the already cached group!
      repository.put(group);
      return rowsUpdated;
    });
  }

  @Override
  public CompletableFuture<Boolean> delete(String name) {
    return getDao().delete(database, name).thenCompose((result) -> {
          if (result) return CompletableFuture.completedFuture(false);
          return Perx.getInstance().getUserGroupService()
              .deleteByGroup(name)
              .thenApply((__) -> true)
              .exceptionally((__) -> true);
        })
        .thenApply((result) -> {
          if (result) repository.remove(name);
          return result;
        });
  }

}
