package io.github.aparx.perx.database.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:20
 * @since 1.0
 */
public interface DatabaseConvertible<T extends DatabaseModel<?>> {

  T toModel();

  /** Pushes this model to the database (performs an update or insert) */
  @CanIgnoreReturnValue
  CompletableFuture<?> push();

}
