package io.github.aparx.perx.database.data;

/**
 * A database model designed after the single responsibility principle, whereas it just serves as
 * a POJO. The handling, controlling and management is done through external implementations.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:19
 * @since 1.0
 */
@FunctionalInterface
public interface DatabaseModel<T> {

  T getId();

}
