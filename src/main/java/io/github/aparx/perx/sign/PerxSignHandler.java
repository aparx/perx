package io.github.aparx.perx.sign;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.utils.BukkitThreads;
import io.github.aparx.perx.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 17:03
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxSignHandler implements Listener, Iterable<PerxSign> {

  private final File file;
  private final SetMultimap<String, PerxSign> signs =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), HashSet::new);

  public PerxSignHandler(@NonNull File file) {
    Preconditions.checkNotNull(file, "File must not be null");
    this.file = file;
  }

  static void save(File file, Set<PerxSign> signs) {
    FileUtils.createFileIfNotExists(file);
    try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
      output.writeObject(signs);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  static void read(File file, Set<PerxSign> signs) {
    if (!file.exists()) return;
    try (ObjectInputStream output = new ObjectInputStream(new FileInputStream(file))) {
      signs.addAll((Set<PerxSign>) output.readObject());
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isValid(Location location) {
    return location.getBlock().getState() instanceof Sign;
  }

  public boolean isSign(Location location) {
    @Nullable World world = location.getWorld();
    return world != null
        && signs.containsKey(world.getName())
        && signs.get(world.getName()).contains(new PerxSign(location));
  }

  public Set<PerxSign> getInWorld(World world) {
    return signs.get(world.getName());
  }

  @CanIgnoreReturnValue
  public boolean add(Location location) {
    return add(new PerxSign(location));
  }

  @CanIgnoreReturnValue
  public boolean add(PerxSign sign) {
    @Nullable Location location = sign.getLocation();
    if (location == null || !isValid(location))
      return false;
    @Nullable World world = location.getWorld();
    return world != null && signs.put(world.getName(), sign);
  }

  @CanIgnoreReturnValue
  public boolean remove(PerxSign sign) {
    @Nullable Location location = sign.getLocation();
    if (location == null) return false;
    @Nullable World world = location.getWorld();
    return world != null && signs.remove(world.getName(), sign);
  }

  @CanIgnoreReturnValue
  public boolean remove(Location location) {
    return remove(new PerxSign(location));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<?> readAsync() {
    if (!file.exists())
      return CompletableFuture.completedFuture(true);
    signs.clear();
    CompletableFuture<?> future = new CompletableFuture<>();
    Bukkit.getScheduler().runTaskAsynchronously(Perx.getPlugin(), () -> {
      Set<PerxSign> output = new HashSet<>();
      read(file, output); // <- this is what should happen asynchronously
      BukkitThreads.runOnPrimaryThread(() -> {
        output.forEach(this::add);
        future.complete(null);
      });
    });
    return future;
  }

  @CanIgnoreReturnValue
  public CompletableFuture<?> saveAsync() {
    CompletableFuture<?> future = new CompletableFuture<>();
    HashSet<PerxSign> signCopy = new HashSet<>(this.signs.values());
    Bukkit.getScheduler().runTaskAsynchronously(Perx.getPlugin(), () -> {
      save(file, signCopy); // <- this is what should happen asynchronously
      BukkitThreads.runOnPrimaryThread(() -> future.complete(null));
    });
    return future;
  }

  @Override
  public Iterator<PerxSign> iterator() {
    return signs.values().iterator();
  }
}
