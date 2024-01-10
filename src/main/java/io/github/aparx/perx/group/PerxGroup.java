package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.data.DatabaseConvertible;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.permission.*;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserService;
import io.github.aparx.perx.utils.BukkitThreads;
import org.apache.commons.lang3.StringUtils;
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
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:31
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxGroup implements DatabaseConvertible<GroupModel>, Comparable<PerxGroup>,
    Iterable<PerxUser> {

  public static final int DEFAULT_PRIORITY = 50;

  private final String name;
  private final EnumMap<GroupStyleKey, @Nullable String> styles;
  private final PerxPermissionRepository repository;

  /** Defines the default state of this group */
  private boolean isDefault;

  /** The lower the priority, the more important this group is */
  private int priority = DEFAULT_PRIORITY;

  private PerxGroup(String name, Function<PerxGroup, PerxPermissionRepository> factory) {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    this.name = transformKey(name);
    this.styles = new EnumMap<>(GroupStyleKey.class);
    PerxPermissionRepository repository = factory.apply(this);
    Preconditions.checkNotNull(repository, "Permission repository must not be null");
    Validate.noNullElements(repository, "Repository must not contain null elements");
    this.repository = repository;
  }

  public static String transformKey(String name) {
    return name.toLowerCase(Locale.ENGLISH);
  }

  public static PerxGroup ofRepository(
      String name, Function<PerxGroup, PerxPermissionRepository> factory) {
    Validate.notEmpty(name, "Group name must not be empty");
    if (StringUtils.containsWhitespace(name))
      throw new IllegalArgumentException("Name must not contain whitespace");
    return new PerxGroup(name, factory);
  }

  public static PerxGroup ofRepository(String name, PerxPermissionRepository repository) {
    return ofRepository(name, (__) -> repository);
  }

  public static PerxGroup ofAdapter(String name, PermissionAdapter adapter) {
    return ofRepository(name, new PerxPermissionMap(adapter));
  }

  public static PerxGroup ofAdapter(String name, Function<PerxGroup, PermissionAdapter> factory) {
    return ofRepository(name, (group) -> new PerxPermissionMap(factory.apply(group)));
  }

  public static PerxGroup of(String name) {
    return ofAdapter(name, Perx.getInstance().getPermissionAdapterFactory()::createAdapter);
  }

  public static PerxGroup of(GroupModel model) {
    return PerxGroupBuilder.builder(model.getId())
        .prefix(model.getPrefix())
        .suffix(model.getSuffix())
        .priority(model.getPriority())
        .setDefault(model.isDefault())
        .addPermissions(model.getPermissions())
        .build();
  }

  public static PerxGroup copyOf(PerxGroup group) {
    PerxGroup copy = ofRepository(group.name, group.repository.copy());
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
    forEachOnline((user, player) -> groupHandler.reinitializePlayer(player));
  }

  public void forEachOnline(BiConsumer<PerxUser, Player> action) {
    BukkitThreads.runOnPrimaryThread(() -> {
      PerxUserService userService = Perx.getInstance().getUserService();
      Bukkit.getOnlinePlayers().forEach((player) -> {
        @Nullable PerxUser user = userService.get(player);
        if (user == null || !user.hasGroup(getName())) return;
        action.accept(user, player);
      });
    });
  }

  public String createCustomName(String playerName) {
    return Perx.getInstance().getGroupHandler().styleExecutor().createDisplayName(this, playerName);
  }

  @Override
  public GroupModel toModel() {
    GroupModel groupModel = new GroupModel(getName());
    groupModel.setPermissions(repository.toPermissionMap());
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

  public PerxPermissionRepository getRepository() {
    return repository;
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
        ", permissions=" + repository +
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
        && Objects.equals(repository, group.repository);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, styles, repository, priority);
  }

  @Override
  public int compareTo(PerxGroup o) {
    return compare(this, o);
  }

  /**
   * Returns a new iterator, that iterates over all concurrent user instances within the cache
   * and filters out all users, that are subscribed to this group.
   *
   * @return a new iterator containing all (cached) users of this group
   */
  @Override
  public Iterator<PerxUser> iterator() {
    Iterator<PerxUser> iterator = Perx.getInstance().getUserService().iterator();
    return new AbstractIterator<>() {
      @Nullable
      @Override
      protected PerxUser computeNext() {
        if (!iterator.hasNext())
          return endOfData();
        @Nullable PerxUser user = iterator.next();
        if (!user.hasGroup(getName()))
          user = computeNext();
        if (user == null)
          return endOfData();
        return user;
      }
    };
  }
}
