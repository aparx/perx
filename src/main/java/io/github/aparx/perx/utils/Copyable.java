package io.github.aparx.perx.utils;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:42
 * @since 1.0
 */
@FunctionalInterface
public interface Copyable<S extends Copyable<S>> {

  /**
   * Returns a shallow copy of the underlying instance.
   *
   * @return a shallow copy of this instance
   */
  S copy();

}
