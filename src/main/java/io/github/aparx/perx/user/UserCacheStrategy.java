package io.github.aparx.perx.user;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 08:23
 * @since 1.0
 */
public enum UserCacheStrategy {
  /** Does not cache the fetched result and just simply returns it. */
  TEMPORARY,
  /** Stored immediately after fetch within an internal cache for as long as not removed */
  RUNTIME,

  /**
   * Automatically stores the fetched user if they are online or are already cached,
   * otherwise it is equivalent to {@link #TEMPORARY}.
   */
  AUTO;
}
