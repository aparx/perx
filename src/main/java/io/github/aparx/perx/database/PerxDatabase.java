package io.github.aparx.perx.database;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import io.github.aparx.perx.Perx;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:49
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxDatabase implements Database {

  private transient final Object lock = new Object();

  private volatile @Nullable ConnectionSource source;
  private DatabaseState state = DatabaseState.DISABLED;

  /** Queue defining consumers that are executed when the database is loaded */
  private final Queue<ThrowingConsumer<PerxDatabase>> queue = new LinkedList<>();

  public DatabaseState getState() {
    return state;
  }

  @Override
  public ConnectionSource getSourceLoudly() {
    @Nullable ConnectionSource source = this.source;
    Preconditions.checkArgument(source != null, "Source is not initialized");
    return source;
  }

  @Override
  public @Nullable ConnectionSource getSource() {
    return source;
  }

  @Override
  public boolean isLoaded() {
    synchronized (lock) {
      return state == DatabaseState.LOADED;
    }
  }

  @Override
  public CompletableFuture<Void> connect(String url, String username, String password) {
    setState(DatabaseState.LOADING);
    CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
    createAsyncTask(future, () -> {
      this.source = new JdbcConnectionSource(url, username, password);
      return null;
    });
    return future.whenComplete((v, t) -> {
      setState(t != null ? DatabaseState.ERROR : DatabaseState.LOADED);
      if (isLoaded()) executeQueue();
      else queue.clear();
    });
  }

  @Override
  public void close() {
    synchronized (lock) {
      @Nullable ConnectionSource source = getSource();
      if (source != null) source.closeQuietly();
    }
  }

  @Override
  @CanIgnoreReturnValue
  public boolean queue(ThrowingConsumer<PerxDatabase> operation) {
    try {
      if (isLoaded()) operation.accept(this);
      else queue.add(operation);
    } catch (Exception e) {
      Perx.getLogger().log(Level.WARNING, "Error in database queue", e);
      throw new RuntimeException(e);
    }
    return true;
  }

  @Override
  @CanIgnoreReturnValue
  public <R> CompletableFuture<R> executeAsync(ThrowingSupplier<R> executor) {
    Preconditions.checkNotNull(executor, "Executor must not be null");
    CompletableFuture<R> future = new CompletableFuture<>();
    queue((__) -> createAsyncTask(future, executor));
    return future;
  }

  @Override
  @CanIgnoreReturnValue
  public CompletableFuture<@Nullable Void> executeAsync(ThrowingRunnable executor) {
    Preconditions.checkNotNull(executor, "Executor must not be null");
    return executeAsync(() -> {
      executor.run();
      return null;
    });
  }

  protected <R> void createAsyncTask(CompletableFuture<R> future, ThrowingSupplier<R> executor) {
    Bukkit.getScheduler().runTaskAsynchronously(Perx.getPlugin(), () -> {
      try {
        final R r;
        synchronized (lock) {
          r = executor.execute();
        }
        future.complete(r);
      } catch (Exception ex) {
        future.completeExceptionally(ex);
      }
    });
  }

  protected void executeQueue() {
    for (@Nullable ThrowingConsumer<PerxDatabase> c; (c = queue.poll()) != null; ) {
      try {
        c.accept(this);
      } catch (Exception e) {
        Perx.getLogger().log(Level.WARNING, "Error in database queue", e);
        throw new RuntimeException(e);
      }
    }
  }

  protected void setState(DatabaseState state) {
    synchronized (lock) {
      this.state = state;
    }
  }


}
