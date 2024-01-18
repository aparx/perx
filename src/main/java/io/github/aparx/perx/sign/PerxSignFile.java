package io.github.aparx.perx.sign;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.utils.BukkitThreads;
import io.github.aparx.perx.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 17:03
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxSignFile implements PerxSignStorage {

  private final File file;
  private final PerxSignRepository repository;

  public PerxSignFile(@NonNull File file, PerxSignRepository repository) {
    Preconditions.checkNotNull(file, "File must not be null");
    Preconditions.checkNotNull(repository, "Repository must not be null");
    this.file = file;
    this.repository = repository;
  }

  static void save(File file, Collection<PerxSign> in) {
    FileUtils.createFileIfNotExists(file);
    try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
      output.writeObject(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static void read(File file, Collection<PerxSign> out) {
    if (!file.exists()) return;
    try (ObjectInputStream output = new ObjectInputStream(new FileInputStream(file))) {
      out.addAll((Set<PerxSign>) output.readObject());
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PerxSignRepository getRepository() {
    return repository;
  }

  @Override
  public CompletableFuture<Void> read() {
    if (!file.exists())
      return CompletableFuture.completedFuture(null);
    repository.clear();
    CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(Perx.getPlugin(), () -> {
      Set<PerxSign> signs = new HashSet<>();
      read(file, signs); // <- this is what should happen asynchronously
      BukkitThreads.runOnPrimaryThread(() -> {
        signs.forEach(repository::add);
        future.complete(null);
      });
    });
    return future;
  }

  @Override
  public CompletableFuture<Void> save() {
    CompletableFuture<@Nullable Void> future = new CompletableFuture<>();
    HashSet<PerxSign> signs = new HashSet<>(repository.toCollection());
    Bukkit.getScheduler().runTaskAsynchronously(Perx.getPlugin(), () -> {
      save(file, signs); // <- this is what should happen asynchronously
      BukkitThreads.runOnPrimaryThread(() -> future.complete(null));
    });
    return future;
  }

  @Override
  public CompletableFuture<Boolean> add(PerxSign sign) {
    if (!repository.add(sign))
      return CompletableFuture.completedFuture(false);
    return save().thenApply((__) -> true);
  }

  @Override
  public CompletableFuture<Boolean> remove(Location location) {
    if (!repository.remove(location))
      return CompletableFuture.completedFuture(false);
    return save().thenApply((__) -> true);
  }
}
