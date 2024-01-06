package io.github.aparx.perx.user.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.group.union.controller.PerxUserGroupManager;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 03:50
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserManager implements PerxUserController {

  private transient final Object lock = new Object();

  protected final Map<UUID, PerxUser> userMap = new HashMap<>();
  protected final Database database;
  protected final PerxUserGroupManager userGroupController;

  public PerxUserManager(Database database, PerxUserGroupManager userGroupController) {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(userGroupController, "Controller must not be null");
    this.database = database;
    this.userGroupController = userGroupController;
  }

  @Override
  public PerxUserManager copy() {
    PerxUserManager manager = new PerxUserManager(database, userGroupController);
    manager.userMap.putAll(userMap);
    return manager;
  }

  @Override
  public CompletableFuture<PerxUser> fetchOrGet(UUID uuid, UserCacheStrategy strategy) {
    if (userMap.containsKey(uuid))
      return CompletableFuture.completedFuture(userMap.get(uuid));
    synchronized (lock) {
      if (userMap.containsKey(uuid))
        return CompletableFuture.completedFuture(userMap.get(uuid));
      return userGroupController.getUserGroupsByUser(uuid).thenApply((userGroups) -> {
        PerxUser user = (userMap.containsKey(uuid) ? userMap.get(uuid) : new PerxUser(uuid));
        userGroups.forEach(user::addGroup);
        UserCacheStrategy strat = strategy;
        if (strat == UserCacheStrategy.AUTO && user.getOffline().isOnline())
          strat = UserCacheStrategy.RUNTIME;
        if (strat == UserCacheStrategy.RUNTIME)
          userMap.put(uuid, user);
        else if (userMap.containsKey(uuid))
          userMap.replace(uuid, user);
        return user;
      });
    }
  }

  public CompletableFuture<PerxUser> fetchOrGet(
      OfflinePlayer player, UserCacheStrategy strategy) {
    return fetchOrGet(player.getUniqueId(), strategy);
  }

  @Override
  public CompletableFuture<Void> delete(UUID uuid) {
    return userGroupController.deleteByUser(uuid).thenAccept((val) -> {
      if (val) remove(uuid);
    });
  }

  @Override
  public CompletableFuture<Void> delete(OfflinePlayer player) {
    return delete(player.getUniqueId());
  }

  @Override
  public CompletableFuture<Void> delete(PerxUser user) {
    return delete(user.getId());
  }

  @Override
  public @Nullable PerxUser get(UUID userId) {
    synchronized (lock) {
      return userMap.get(userId);
    }
  }

  @Override
  public @Nullable PerxUser get(OfflinePlayer player) {
    synchronized (lock) {
      return userMap.get(player.getUniqueId());
    }
  }

  @Override
  public boolean contains(UUID uuid) {
    synchronized (lock) {
      return userMap.containsKey(uuid);
    }
  }

  @Override
  public boolean contains(OfflinePlayer player) {
    synchronized (lock) {
      return userMap.containsKey(player.getUniqueId());
    }
  }

  @Override
  public boolean remove(PerxUser user) {
    synchronized (lock) {
      return userMap.remove(user.getId(), user);
    }
  }

  @Override
  public boolean remove(UUID uuid) {
    synchronized (lock) {
      return userMap.remove(uuid) != null;
    }
  }

  @Override
  public boolean remove(OfflinePlayer player) {
    synchronized (lock) {
      return userMap.remove(player.getUniqueId()) != null;
    }
  }

  @Override
  public Iterator<PerxUser> iterator() {
    synchronized (lock) {
      Iterator<PerxUser> iterator = userMap.values().iterator();
      return new AbstractIterator<>() {
        @Nullable
        @Override
        protected PerxUser computeNext() {
          if (!iterator.hasNext())
            return endOfData();
          return iterator.next();
        }
      };
    }
  }
}
