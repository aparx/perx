package io.github.aparx.perx.user;

import io.github.aparx.perx.utils.Copyable;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A simple map-like data structure that stores {@code PerxUser} instances.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 03:46
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxUserService extends Copyable<PerxUserService> {

  PerxUserRepository getRepository();

  /** Returns the already cached user or fetches user with given identifier. */
  CompletableFuture<PerxUser> getOrFetch(UUID uuid, UserCacheStrategy strategy);

  /** Returns the already cached user or fetches given player's user profile. */
  CompletableFuture<PerxUser> getOrFetch(OfflinePlayer player, UserCacheStrategy strategy);

  CompletableFuture<Void> delete(UUID uuid);

  CompletableFuture<Void> delete(OfflinePlayer player);

  CompletableFuture<Void> delete(PerxUser user);

}
