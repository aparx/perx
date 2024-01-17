package io.github.aparx.perx.user;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.aliasing.qual.NonLeaked;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 00:18
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxUserRepository extends Iterable<PerxUser> {

  @CanIgnoreReturnValue
  boolean add(PerxUser user);

  @CanIgnoreReturnValue
  @Nullable PerxUser replace(PerxUser user);

  @Nullable PerxUser get(UUID userId);

  @Nullable PerxUser get(OfflinePlayer player);

  boolean contains(UUID uuid);

  boolean contains(OfflinePlayer player);

  @CanIgnoreReturnValue
  boolean remove(PerxUser user);

  @CanIgnoreReturnValue
  boolean remove(UUID uuid);

  @CanIgnoreReturnValue
  boolean remove(OfflinePlayer player);

}
