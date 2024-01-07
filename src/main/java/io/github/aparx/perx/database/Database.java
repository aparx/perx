package io.github.aparx.perx.database;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.support.ConnectionSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 07:04
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface Database {

  DatabaseState getState();

  ConnectionSource getSourceLoudly();

  @Nullable ConnectionSource getSource();

  boolean isLoaded();

  CompletableFuture<Void> connect(String url, String username, String password);

  void close();

  @CanIgnoreReturnValue
  boolean queue(ThrowingConsumer<PerxDatabase> operation);

  @CanIgnoreReturnValue
  <R> CompletableFuture<R> executeAsync(ThrowingSupplier<R> executor);

  @CanIgnoreReturnValue
  CompletableFuture<@Nullable Void> executeAsync(ThrowingRunnable executor);

  interface ThrowingSupplier<R> {
    R execute() throws Exception;
  }

  interface ThrowingRunnable {
    void run() throws Exception;
  }

  interface ThrowingConsumer<T> {
    void accept(T value) throws Exception;
  }

}
