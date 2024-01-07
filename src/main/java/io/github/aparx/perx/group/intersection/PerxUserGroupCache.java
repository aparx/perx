package io.github.aparx.perx.group.intersection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.utils.BukkitThreads;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 06:37
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserGroupCache implements PerxUserGroupRepository, Iterable<PerxUserGroup> {

  private final ListMultimap<UUID, PerxUserGroup> byUser =
      Multimaps.newListMultimap(new ConcurrentHashMap<>(), ArrayList::new);

  private final ListMultimap<String, PerxUserGroup> byGroup =
      Multimaps.newListMultimap(new ConcurrentHashMap<>(), ArrayList::new);

  private final Map<Long, PerxUserGroup> byId = new ConcurrentHashMap<>();

  @Override
  public List<PerxUserGroup> findByUser(UUID userId) {
    Preconditions.checkNotNull(userId, "ID must not be null");
    return byUser.get(userId);
  }

  @Override
  public List<PerxUserGroup> findByGroup(String groupName) {
    Preconditions.checkNotNull(groupName, "Name must not be null");
    return byGroup.get(PerxGroup.transformKey(groupName));
  }

  @Override
  public @Nullable PerxUserGroup findById(long userGroupId) {
    return byId.get(userGroupId);
  }

  @Override
  public boolean hasUser(UUID userId) {
    return byUser.containsKey(userId);
  }

  @Override
  public void removeByGroup(String groupName) {
    final String group = PerxGroup.transformKey(groupName);
    BukkitThreads.runOnPrimaryThread(() -> {
      byGroup.get(group).forEach((userGroup) -> {
        userGroup.markRemoved();
        byUser.remove(userGroup.getUserId(), userGroup);
        byId.remove(userGroup.getId());
      });
      byGroup.removeAll(group);
    });
  }

  @Override
  public void removeByUser(UUID userId) {
    BukkitThreads.runOnPrimaryThread(() -> {
      byUser.get(userId).forEach((userGroup) -> {
        @Nullable PerxGroup group = userGroup.findGroup();
        if (group != null)
          byGroup.remove(group.getName(), userGroup);
        byId.remove(userGroup.getId());
      });
      byUser.removeAll(userId);
    });
  }

  @Override
  public void removeById(long userGroupId) {
    BukkitThreads.runOnPrimaryThread(() -> {
      @Nullable PerxUserGroup userGroup = byId.get(userGroupId);
      if (userGroup == null) return;
      byUser.remove(userGroup.getUserId(), userGroup);
      @Nullable PerxGroup group = userGroup.findGroup();
      if (group != null) byGroup.remove(group.getName(), userGroup);
      byId.remove(userGroupId);
    });
  }

  @Override
  public boolean put(PerxUserGroup userGroup) {
    Preconditions.checkNotNull(userGroup, "User group must not be null");
    @Nullable PerxGroup group = userGroup.findGroup();
    if (group == null) return false;
    BukkitThreads.runOnPrimaryThread(() -> {
      byUser.put(userGroup.getUserId(), userGroup);
      byId.put(userGroup.getId(), userGroup);
      byGroup.put(group.getName(), userGroup);
    });
    return true;
  }

  @Override
  public Iterator<PerxUserGroup> iterator() {
    Iterator<PerxUserGroup> iterator = byGroup.values().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public PerxUserGroup next() {
        return iterator.next();
      }
    };
  }
}
