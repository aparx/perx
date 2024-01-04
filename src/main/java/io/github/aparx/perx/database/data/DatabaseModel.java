package io.github.aparx.perx.database.data;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:19
 * @since 1.0
 */
@FunctionalInterface
public interface DatabaseModel<T> {

  T getId();

}
