package io.github.aparx.perx.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.group.union.PerxUserGroup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 03:38
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUser implements Iterable<PerxUserGroup> {

  private final UUID id;

  /** Set of group names this user is subscribed to */
  private final Map<String, PerxUserGroup> subscribed;

  public PerxUser(UUID id) {
    this(id, new ConcurrentHashMap<>());
  }

  private PerxUser(UUID id, Map<String, PerxUserGroup> subscribed) {
    Preconditions.checkNotNull(id, "ID must not be null");
    Preconditions.checkNotNull(subscribed, "Set must not be null");
    this.id = id;
    this.subscribed = subscribed;
  }

  public UUID getId() {
    return id;
  }

  public OfflinePlayer getOffline() {
    return Bukkit.getOfflinePlayer(id);
  }

  public @Nullable Player getPlayer() {
    return Bukkit.getPlayer(id);
  }

  @CanIgnoreReturnValue
  public @Nullable PerxUserGroup addGroup(PerxUserGroup group) {
    return subscribed.put(group.getGroup().getName(), group);
  }

  @CanIgnoreReturnValue
  public boolean removeGroup(PerxUserGroup group) {
    return subscribed.remove(group.getGroup().getName(), group);
  }

  @CanIgnoreReturnValue
  public @Nullable PerxUserGroup removeGroup(String groupName) {
    return subscribed.remove(groupName);
  }

  public boolean hasGroup(String groupName) {
    return subscribed.containsKey(groupName);
  }

  /**
   * Returns the user group whose group has the highest priority.
   * <p>This is helpful to determine, what the display name of a player is going to be.
   *
   * @return an optional representing the concurrently highest group of this user
   */
  public Optional<PerxUserGroup> getHighestUserGroup() {
    return getSubscribed().stream().max(PerxUserGroup::compare);
  }

  public Optional<PerxUserGroup> getHighestUserGroup(Predicate<PerxUserGroup> filter) {
    return getSubscribed().stream().filter(filter).max(PerxUserGroup::compare);
  }

  public Collection<PerxUserGroup> getSubscribed() {
    return subscribed.values();
  }

  @Override
  public Iterator<PerxUserGroup> iterator() {
    Iterator<PerxUserGroup> iterator = subscribed.values().iterator();
    return new AbstractIterator<>() {
      @Nullable
      @Override
      protected PerxUserGroup computeNext() {
        if (!iterator.hasNext())
          return endOfData();
        PerxUserGroup next = iterator.next();
        if (next.isGroupValid()) return next;
        Perx.getInstance().getUserGroupController().removeById(next.getId());
        iterator.remove();
        return computeNext();
      }
    };
  }

  @Override
  public String toString() {
    return "PerxUser{" +
        "id=" + id +
        ", subscribed=" + subscribed +
        '}';
  }
}
