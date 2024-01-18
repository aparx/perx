package io.github.aparx.perx.sign;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 01:11
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxSignStorage {

  PerxSignRepository getRepository();

  @CanIgnoreReturnValue
  CompletableFuture<Void> read();

  @CanIgnoreReturnValue
  CompletableFuture<Void> save();

  @CanIgnoreReturnValue
  CompletableFuture<Boolean> add(PerxSign sign);

  @CanIgnoreReturnValue
  CompletableFuture<Boolean> remove(Location location);

  static PerxSignStorage ofFile(File file, PerxSignRepository repository) {
    return new PerxSignFile(file, repository);
  }

  static PerxSignStorage ofFile(File file) {
    return ofFile(file, new PerxSignCache());
  }

}
