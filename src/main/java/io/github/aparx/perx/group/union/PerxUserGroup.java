package io.github.aparx.perx.group.union;

import com.google.common.base.Preconditions;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.data.DatabaseConvertible;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.PerxGroup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 23:53
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUserGroup implements DatabaseConvertible<UserGroupModel>,
    Comparable<PerxUserGroup> {

  public static Comparator<PerxUserGroup> USER_GROUP_COMPARATOR = PerxUserGroup::compare;

  private static final long CACHE_ONLY_ID = -1;

  private final UserGroupModel model;

  private final String groupName;
  private final Reference<PerxGroup> group;

  private volatile boolean removed;

  protected PerxUserGroup(UserGroupModel model, PerxGroup group) {
    Preconditions.checkNotNull(model, "Model must not be null");
    Preconditions.checkNotNull(group, "Group must not be null");
    this.model = model;
    this.groupName = group.getName();
    this.group = new WeakReference<>(group);
  }

  protected PerxUserGroup(long id, UUID userId, PerxGroup group) {
    Preconditions.checkNotNull(userId, "User must not be null");
    Preconditions.checkNotNull(group, "Group must not be null");
    this.group = new WeakReference<>(group);
    this.groupName = group.getName();
    this.model = new UserGroupModel(userId, null);
    this.model.setId(id);
  }

  public static PerxUserGroup of(UserGroupModel model) {
    return new PerxUserGroup(model, PerxGroup.of(model.getGroup()));
  }

  public static PerxUserGroup of(UserGroupModel model, PerxGroup group) {
    return new PerxUserGroup(model, group);
  }

  public static PerxUserGroup of(UUID userId, PerxGroup group) {
    return of(CACHE_ONLY_ID, userId, group);
  }

  public static PerxUserGroup of(long id, UUID userId, PerxGroup group) {
    return new PerxUserGroup(id, userId, group);
  }

  public static int compare(PerxUserGroup a, PerxUserGroup b) {
    @Nullable PerxGroup aGroup = a.findGroup();
    @Nullable PerxGroup bGroup = b.findGroup();
    if (aGroup == null)
      return bGroup == null ? 0 : -1;
    return (bGroup != null ? PerxGroup.compare(aGroup, bGroup) : 1);
  }

  /** Returns true if this group has a valid ID that matches a database model */
  public boolean isModelInDatabase() {
    return getId() != CACHE_ONLY_ID;
  }

  public long getId() {
    return model.getId();
  }

  public void setId(long id) {
    model.setId(id);
  }

  public UUID getUserId() {
    return model.getUserId();
  }

  public @Nullable Date getEndingDate() {
    return model.getEndDate();
  }

  public void setEndingDate(@Nullable Date endingDate) {
    model.setEndDate(endingDate);
  }

  public PerxGroup getGroup() {
    @Nullable PerxGroup group = findGroup();
    Preconditions.checkArgument(group != null, "Group has become invalid");
    return group;
  }

  public @Nullable PerxGroup findGroup() {
    return group.get();
  }

  public String getGroupName() {
    return groupName;
  }

  public boolean isGroupValid() {
    return group.get() != null;
  }

  @Override
  public UserGroupModel toModel() {
    model.setGroup(getGroup().toModel());
    model.setEndDate(getEndingDate());
    return model;
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> push() {
    return Perx.getInstance().getUserGroupController().upsert(this);
  }

  @Override
  public CompletableFuture<?> update() {
    return Perx.getInstance().getUserGroupController().update(this);
  }

  @Override
  public CompletableFuture<Boolean> delete() {
    Preconditions.checkState(isModelInDatabase(), "Not a database model");
    return Perx.getInstance().getUserGroupController().deleteById(getId());
  }

  public void deleteRemovalMark() {
    synchronized (this) {
      removed = false;
    }
  }

  public void markRemoved() {
    synchronized (this) {
      removed = true;
    }
  }

  public boolean isMarkedRemoved() {
    synchronized (this) {
      return removed;
    }
  }

  @Override
  public String toString() {
    return "PerxUserGroup{" +
        "model=" + model +
        ", groupName='" + groupName + '\'' +
        ", group=" + group.get() +
        '}';
  }

  @Override
  public int compareTo(PerxUserGroup o) {
    return compare(this, o);
  }
}
