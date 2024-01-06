package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.data.DatabaseConvertible;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.permission.*;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.BukkitThreads;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:31
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxGroup implements DatabaseConvertible<GroupModel>, Comparable<PerxGroup> {

  public static final int DEFAULT_PRIORITY = 50;

  private final String name;
  private final EnumMap<GroupStyleKey, @Nullable String> styles;
  private final PerxPermissionRegister permissions;

  /** Defines the default state of this group */
  private boolean isDefault;

  /** The lower the priority, the more important this group is */
  private int priority = DEFAULT_PRIORITY;

  private PerxGroup(String name, PerxPermissionRegister permissions) {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkNotNull(permissions, "Permissions must not be null");
    this.name = formatName(name);
    this.styles = new EnumMap<>(GroupStyleKey.class);
    this.permissions = permissions;
  }

  public static String formatName(String name) {
    return name.toLowerCase(Locale.ENGLISH);
  }

  public static PerxGroup of(String name, PerxPermissionRegister register) {
    Validate.notEmpty(name, "Group name must not be empty");
    Validate.noNullElements(register, "Permission must not be null");
    return new PerxGroup(name, register);
  }

  public static PerxGroup of(String name, PermissionAdapter adapter) {
    return of(name, new PerxPermissionMap(adapter));
  }

  public static PerxGroup of(String name) {
    // TODO put mutable factory within the current Perx instance
    return of(name, new AttachingPermissionAdapter(Perx.getPlugin()));
  }

  public static PerxGroup of(GroupModel model) {
    return PerxGroupBuilder.builder(model.getId())
        .prefix(model.getPrefix())
        .suffix(model.getSuffix())
        .priority(model.getPriority())
        .isDefault(model.isDefault())
        .addPermissions(model.getPermissions())
        .build();
  }

  public static PerxGroup copyOf(PerxGroup group) {
    PerxGroup copy = of(group.name, group.permissions.copy());
    copy.setPriority(group.getPriority());
    copy.setDefault(group.isDefault());
    copy.styles.putAll(group.styles);
    return copy;
  }

  public static int compare(PerxGroup a, PerxGroup b) {
    return Integer.compare(b.priority, a.priority);
  }

  public PerxGroup copy() {
    return copyOf(this);
  }

  public String getName() {
    return name;
  }

  public void updatePlayers() {
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    forPlayers((user, player) -> groupHandler.reinitializePlayer(player));
  }

  public void forSubscribers(Consumer<PerxUser> action) {
    Perx.getInstance().getUserController().forEach((user) -> {
      if (user.hasGroup(getName()))
        action.accept(user);
    });
  }

  public void forPlayers(BiConsumer<PerxUser, Player> action) {
    BukkitThreads.runOnPrimaryThread(() -> {
      PerxUserController userController = Perx.getInstance().getUserController();
      Bukkit.getOnlinePlayers().forEach((player) -> {
        @Nullable PerxUser user = userController.get(player);
        if (user == null || !user.hasGroup(getName())) return;
        action.accept(user, player);
      });
    });
  }

  public String createCustomName(String playerName) {
    StringBuilder builder = new StringBuilder();
    if (hasStyle(GroupStyleKey.PREFIX))
      builder.append(getStyle(GroupStyleKey.PREFIX));
    builder.append(playerName);
    if (hasStyle(GroupStyleKey.SUFFIX))
      builder.append(getStyle(GroupStyleKey.SUFFIX));
    return builder.toString();
  }

  @Override
  public GroupModel toModel() {
    GroupModel groupModel = new GroupModel(getName());
    String[] permissions = new String[this.permissions.size()];
    Iterator<PerxPermission> itr = this.permissions.iterator();
    int cursor = 0;
    for (; itr.hasNext() && cursor < permissions.length; ++cursor)
      permissions[cursor] = itr.next().getName();
    groupModel.setPermissions((cursor != permissions.length
        ? Arrays.copyOf(permissions, cursor)
        : permissions));
    groupModel.setPrefix(getStyle(GroupStyleKey.PREFIX));
    groupModel.setSuffix(getStyle(GroupStyleKey.SUFFIX));
    groupModel.setDefault(isDefault());
    groupModel.setPriority(getPriority());
    return groupModel;
  }

  @Override
  public CompletableFuture<Dao.CreateOrUpdateStatus> push() {
    return Perx.getInstance().getGroupHandler().upsert(this).thenApply((status) -> {
      Perx.getLogger().log(Level.INFO, String.format(
          "Successfully pushed group '%s' (default: %s)",
          getName(), isDefault()));
      return status;
    });
  }

  @Override
  public CompletableFuture<Integer> update() {
    return Perx.getInstance().getGroupHandler().update(this);
  }

  @Override
  public CompletableFuture<Boolean> delete() {
    return Perx.getInstance().getGroupHandler().delete(this);
  }

  @CanIgnoreReturnValue
  public @Nullable String setStyle(GroupStyleKey key, @Nullable String value) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return (value != null
        ? styles.put(key, ChatColor.translateAlternateColorCodes('&', value))
        : styles.remove(key));
  }

  public @Nullable String getStyle(GroupStyleKey key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return styles.get(key);
  }

  public boolean hasStyle() {
    return Arrays.stream(GroupStyleKey.values()).anyMatch(this::hasStyle);
  }

  public boolean hasStyle(GroupStyleKey key) {
    return styles.get(key) != null;
  }

  public PerxPermissionRegister getPermissions() {
    return permissions;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  @Override
  public String toString() {
    return "PerxGroup{" +
        "name='" + name + '\'' +
        ", styles=" + styles +
        ", permissions=" + permissions +
        ", defaultValue=" + isDefault +
        ", priority=" + priority +
        '}';
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    PerxGroup group = (PerxGroup) object;
    return priority == group.priority
        && isDefault == group.isDefault
        && Objects.equals(name, group.name)
        && Objects.equals(styles, group.styles)
        && Objects.equals(permissions, group.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, styles, permissions, priority);
  }

  @Override
  public int compareTo(PerxGroup o) {
    return compare(this, o);
  }

}
