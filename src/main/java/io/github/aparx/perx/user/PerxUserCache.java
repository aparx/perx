package io.github.aparx.perx.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 00:18
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserCache implements PerxUserRepository {


  private final transient Object mutex;

  private final Map<UUID, PerxUser> userMap = new HashMap<>();

  public PerxUserCache() {
    this(new Object());
  }

  public PerxUserCache(Object mutex) {
    Preconditions.checkNotNull(mutex, "Lock must not be null");
    this.mutex = mutex;
  }

  @Override
  public boolean add(PerxUser user) {
    return userMap.putIfAbsent(user.getId(), user) == null;
  }

  @Override
  public @Nullable PerxUser replace(PerxUser user) {
    return userMap.replace(user.getId(), user);
  }

  @Override
  public @Nullable PerxUser get(UUID userId) {
    synchronized (mutex) {
      return userMap.get(userId);
    }
  }

  @Override
  public @Nullable PerxUser get(OfflinePlayer player) {
    synchronized (mutex) {
      return userMap.get(player.getUniqueId());
    }
  }

  @Override
  public boolean contains(UUID uuid) {
    synchronized (mutex) {
      return userMap.containsKey(uuid);
    }
  }

  @Override
  public boolean contains(OfflinePlayer player) {
    synchronized (mutex) {
      return userMap.containsKey(player.getUniqueId());
    }
  }

  @Override
  public boolean remove(PerxUser user) {
    synchronized (mutex) {
      return userMap.remove(user.getId(), user);
    }
  }

  @Override
  public boolean remove(UUID uuid) {
    synchronized (mutex) {
      return userMap.remove(uuid) != null;
    }
  }

  @Override
  public boolean remove(OfflinePlayer player) {
    synchronized (mutex) {
      return userMap.remove(player.getUniqueId()) != null;
    }
  }

  @Override
  public Iterator<PerxUser> iterator() {
    synchronized (mutex) {
      Iterator<PerxUser> iterator = userMap.values().iterator();
      return new AbstractIterator<>() {
        @Nullable
        @Override
        protected PerxUser computeNext() {
          if (!iterator.hasNext())
            return endOfData();
          return iterator.next();
        }
      };
    }
  }
}
