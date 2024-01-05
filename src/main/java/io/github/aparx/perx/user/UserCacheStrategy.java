package io.github.aparx.perx.user;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 08:23
 * @since 1.0
 */
public enum UserCacheStrategy {
  /**
   * Does not cache a fetched user at all.
   * <p>This implies, that a stale version of the user in cache will not be replaced by a fresh.
   */
  TEMPORARY,

  /**
   * Fetched users are stored immediately and stale instances are replaced.
   * <p>The lifetime of a fetched user with this strategy is as long as the strategy of its
   * managing controller and as long as it is not removed manually.
   */
  RUNTIME,

  /**
   * Stores the fetched user for runtime, if they are online or already cached. Otherwise, the
   * fetched user is not cached and stale instances of the user not replaced.
   * <p>This means, that {@link #RUNTIME} is used for when the fetched user is currently online
   * or is already cached. Otherwise, {@link #TEMPORARY} is used.
   *
   * @see #TEMPORARY
   * @see #RUNTIME
   */
  AUTO
}
