package io.github.aparx.perx.user;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.group.intersection.PerxUserGroupService;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 03:50
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserManager implements PerxUserService {

  protected transient final Object mutex = new Object();

  private final PerxUserRepository repository;
  private final Database database;
  private final PerxUserGroupService userGroupService;

  public PerxUserManager(Database database, PerxUserGroupService userGroupService) {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(userGroupService, "Service must not be null");
    this.database = database;
    this.userGroupService = userGroupService;
    this.repository = new PerxUserCache(mutex);
  }

  @Override
  public PerxUserManager copy() {
    PerxUserManager manager = new PerxUserManager(database, userGroupService);
    repository.forEach(manager.repository::add);
    return manager;
  }

  @Override
  public PerxUserRepository getRepository() {
    return repository;
  }

  @Override
  public CompletableFuture<PerxUser> getOrFetch(UUID uuid, UserCacheStrategy strategy) {
    if (repository.contains(uuid))
      return CompletableFuture.completedFuture(repository.get(uuid));
    synchronized (mutex) {
      if (repository.contains(uuid))
        return CompletableFuture.completedFuture(repository.get(uuid));
      return userGroupService.getUserGroupsByUser(uuid).thenApply((userGroups) -> {
        @Nullable PerxUser user = repository.get(uuid);
        if (user == null) user = new PerxUser(uuid);
        userGroups.forEach(user::addGroup);
        UserCacheStrategy strat = strategy;
        if (strat == UserCacheStrategy.AUTO && user.getOffline().isOnline())
          strat = UserCacheStrategy.RUNTIME;
        if (strat == UserCacheStrategy.RUNTIME)
          repository.add(user);
        else if (repository.contains(uuid))
          repository.replace(user);
        return user;
      });
    }
  }

  public CompletableFuture<PerxUser> getOrFetch(OfflinePlayer player, UserCacheStrategy strategy) {
    return getOrFetch(player.getUniqueId(), strategy);
  }

  @Override
  public CompletableFuture<Void> delete(UUID uuid) {
    return userGroupService.deleteByUser(uuid).thenAccept((val) -> {
      if (val) repository.remove(uuid);
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

}
