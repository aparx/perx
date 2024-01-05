package io.github.aparx.perx.group.controller;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.group.PerxGroup;
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
public class PerxGroupManager implements PerxGroupController {

  private final Map<String, PerxGroup> groupMap = new ConcurrentHashMap<>();
  private @Nullable Dao<GroupModel, String> dao;

  private final Database database;

  public PerxGroupManager(Database database) {
    Preconditions.checkNotNull(database, "Database must not be null");
    this.database = database;
  }

  @Override
  public void load() {
    // TODO re-fetch
    database.executeAsync(() -> {
      ConnectionSource dbSource = database.getSourceLoudly();
      this.dao = DaoManager.createDao(dbSource, GroupModel.class);
      TableUtils.createTableIfNotExists(dbSource, GroupModel.class);
      dao.queryForAll().stream().filter(this::registerModel).forEach((model) -> {
        Perx.getLogger().log(Level.INFO, "Registered group {0}", model.getId());
      });
      Bukkit.getScheduler().runTask(Perx.getPlugin(), () -> {
        Bukkit.getPluginManager().callEvent(new GroupsFetchedEvent(this, false));
      });
    });
  }

  @CanIgnoreReturnValue
  public boolean registerModel(GroupModel model) {
    return register(PerxGroup.of(model));
  }

  @Override
  public Dao<GroupModel, String> getDao() {
    @Nullable Dao<GroupModel, String> dao = this.dao;
    Preconditions.checkArgument(dao != null, "DAO is not initialized");
    return dao;
  }

  @Override
  public PerxGroupManager copy() {
    PerxGroupManager register = new PerxGroupManager(database);
    register.groupMap.putAll(groupMap);
    register.dao = dao;
    return register;
  }

  @Override
  public CompletableFuture<Boolean> create(PerxGroup group) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    database.executeAsync(() -> getDao().create(group.toModel()))
        .thenApply((i) -> (i >= 1 && register(group)))
        .whenComplete((status, ex) -> {
          if (ex != null) future.completeExceptionally(ex);
          else future.complete(status);
        });
    return future;
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group) {
    CompletableFuture<Dao.CreateOrUpdateStatus> future = new CompletableFuture<>();
    database.executeAsync(() -> getDao().createOrUpdate(group.toModel()))
        .thenApply((status) -> {
          // ensure `group` is registered
          groupMap.put(group.getName(), group);
          return status;
        })
        .whenComplete((status, ex) -> {
          if (ex != null) future.completeExceptionally(ex);
          else future.complete(status);
        });
    return future;
  }

  @Override
  public CompletableFuture<Boolean> delete(String name) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    database.executeAsync(() -> getDao().deleteById(name))
        .thenApply((x) -> {
          if (x < 1) return false;
          remove(name);
          return true;
        })
        .whenComplete((val, ex) -> {
          if (ex != null) future.completeExceptionally(ex);
          else future.complete(val);
        });
    return future;
  }

  @Override
  public int size() {
    return groupMap.size();
  }

  @Override
  public boolean register(PerxGroup group) {
    return groupMap.putIfAbsent(group.getName(), group) == null;
  }

  @Override
  public boolean remove(PerxGroup group) {
    return groupMap.remove(group.getName(), group);
  }

  @Override
  public @Nullable PerxGroup remove(String name) {
    // TODO remove all user groups from groups with given name
    name = PerxGroup.formatName(name);
    @Nullable PerxGroup removed = groupMap.remove(name);
    if (removed == null) return null;
    Perx.getInstance().getUserGroupController().removeByGroup(name);
    return removed;
  }

  @Override
  public boolean contains(PerxGroup group) {
    return group.equals(groupMap.get(group.getName()));
  }

  @Override
  public boolean contains(String name) {
    return groupMap.containsKey(PerxGroup.formatName(name));
  }

  @Override
  public Collection<PerxGroup> getDefaults() {
    return stream().filter(PerxGroup::isDefault).collect(Collectors.toList());
  }

  @Override
  public @Nullable PerxGroup get(String name) {
    return groupMap.get(PerxGroup.formatName(name));
  }

  @Override
  public Iterator<PerxGroup> iterator() {
    return groupMap.values().iterator();
  }

  public Stream<PerxGroup> stream() {
    return groupMap.values().stream();
  }
}
