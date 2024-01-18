package io.github.aparx.perx.sign;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.Set;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 00:54
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxSignRepository extends Iterable<PerxSign> {

  int size();

  boolean isEmpty();

  void clear();

  Set<PerxSign> getInWorld(World world);

  @CanIgnoreReturnValue
  boolean add(Location location);

  @CanIgnoreReturnValue
  boolean add(PerxSign sign);

  @CanIgnoreReturnValue
  boolean remove(PerxSign sign);

  @CanIgnoreReturnValue
  boolean remove(Location location);

  boolean contains(Location location);

  Collection<PerxSign> toCollection();

}
