package io.github.aparx.perx.sign;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 00:54
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxSignCache implements PerxSignRepository {

  private final SetMultimap<String, PerxSign> signs =
      Multimaps.newSetMultimap(new ConcurrentHashMap<>(), HashSet::new);

  @Override
  public int size() {
    return signs.size();
  }

  @Override
  public boolean isEmpty() {
    return signs.isEmpty();
  }

  @Override
  public void clear() {
    signs.clear();
  }

  public Set<PerxSign> getInWorld(World world) {
    return signs.get(world.getName());
  }

  @CanIgnoreReturnValue
  public boolean add(Location location) {
    return add(PerxSign.of(location));
  }

  @CanIgnoreReturnValue
  public boolean add(PerxSign sign) {
    @Nullable Location location = sign.getLocation();
    if (location == null) return false;
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
    return remove(PerxSign.of(location));
  }

  @Override
  public boolean contains(Location location) {
    @Nullable World world = location.getWorld();
    if (world == null) return false;
    return signs.containsKey(world.getName())
        && signs.get(world.getName()).contains(PerxSign.of(location));
  }

  @Override
  public Collection<PerxSign> toCollection() {
    return signs.values();
  }

  @Override
  public @NonNull Iterator<PerxSign> iterator() {
    return signs.values().iterator();
  }
}
