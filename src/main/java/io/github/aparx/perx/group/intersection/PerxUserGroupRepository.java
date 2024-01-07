package io.github.aparx.perx.group.intersection;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 06:36
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface PerxUserGroupRepository {

  List<PerxUserGroup> findByUser(UUID userId);

  List<PerxUserGroup> findByGroup(String groupName);

  @Nullable PerxUserGroup findById(long userGroupId);

  boolean hasUser(UUID userId);

  void removeByUser(UUID userId);

  void removeByGroup(String groupName);

  void removeById(long userGroupId);

  /**
   * Registers and potentially overrides any already similarly registered group in the cache.
   * <p>This method only accesses the cache. Thus, it has no effect on long term storage.
   *
   * @param userGroup the user group to be introduced to the cache
   * @return true if the user group could be registered
   */
  @CanIgnoreReturnValue
  boolean put(PerxUserGroup userGroup);

}
