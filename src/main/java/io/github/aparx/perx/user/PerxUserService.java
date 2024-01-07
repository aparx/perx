package io.github.aparx.perx.user;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.utils.Copyable;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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
public interface PerxUserService extends Copyable<PerxUserService>, Iterable<PerxUser> {

  /** Returns the already cached user or fetches user with given identifier. */
  CompletableFuture<PerxUser> fetchOrGet(UUID uuid, UserCacheStrategy strategy);

  /** Returns the already cached user or fetches given player's user profile. */
  CompletableFuture<PerxUser> fetchOrGet(OfflinePlayer player, UserCacheStrategy strategy);

  CompletableFuture<Void> delete(UUID uuid);

  CompletableFuture<Void> delete(OfflinePlayer player);

  CompletableFuture<Void> delete(PerxUser user);

  @Nullable PerxUser get(UUID userId);

  @Nullable PerxUser get(OfflinePlayer player);

  boolean contains(UUID uuid);

  boolean contains(OfflinePlayer player);

  @CanIgnoreReturnValue
  boolean remove(PerxUser user);

  @CanIgnoreReturnValue
  boolean remove(UUID uuid);

  @CanIgnoreReturnValue
  boolean remove(OfflinePlayer player);

}
