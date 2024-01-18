package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.data.DatabaseModelConvertible;
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
 * Permission group that bundles permissions and styles into a named entity.
 * <p>This object differs from the {@link GroupModel}, as in that this is actually used and
 * interacted with at runtime, whereas the model is just a database representation of this object
 * (POJO).
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:31
 * @see GroupModel
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxGroup implements DatabaseModelConvertible<GroupModel>,
    Comparable<PerxGroup>, Iterable<PerxUser> {

  public static final int DEFAULT_PRIORITY = 50;

  private final EnumMap<GroupStyleKey, @Nullable String> styles;
  private final PerxPermissionRepository permissionRepository;

  private final GroupModel model;

  private PerxGroup(String name, Function<PerxGroup, PerxPermissionRepository> factory) {
    Preconditions.checkNotNull(name, "Name must not be null");
    Preconditions.checkNotNull(factory, "Factory must not be null");
    this.model = new GroupModel(transformKey(name));
    this.styles = new EnumMap<>(GroupStyleKey.class);
    PerxPermissionRepository repository = factory.apply(this);
    Preconditions.checkNotNull(repository, "Permission repository must not be null");
    Validate.noNullElements(repository, "Repository must not contain null elements");
    this.permissionRepository = repository;
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
    PerxGroup copy = ofRepository(group.getName(), group.permissionRepository.copy());
    copy.setPriority(group.getPriority());
    copy.setDefault(group.isDefault());
    copy.styles.putAll(group.styles);
    return copy;
  }

  public static int compare(PerxGroup a, PerxGroup b) {
    return Integer.compare(b.getPriority(), a.getPriority());
  }

  public PerxGroup copy() {
    return copyOf(this);
  }

  public String getName() {
    return model.getId();
  }

  /**
   * Updates all players of this group in the current or next tick of the primary thread.
   * <p>The update is defined such that all online players of this group will be reinitialized.
   */
  public void updatePlayers() {
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    forEachOnline((user, player) -> groupHandler.reinitializePlayer(player));
  }

  /**
   * Iterates over every online player that subscribes to this group and calls {@code action} on
   * each that has been located successfully.
   * <p>The iteration happens on the current or next tick of the primary thread.
   *
   * @param action the callback responsible for handling the online subscribers
   */
  public void forEachOnline(BiConsumer<PerxUser, Player> action) {
    BukkitThreads.runOnPrimaryThread(() -> {
      PerxUserService userService = Perx.getInstance().getUserService();
      Bukkit.getOnlinePlayers().forEach((player) -> {
        @Nullable PerxUser user = userService.getRepository().get(player);
        if (user == null || !user.hasGroup(getName())) return;
        action.accept(user, player);
      });
    });
  }

  /**
   * Creates a new display name using given {@code entityName}.
   *
   * @param entityName the (custom) name of the entity, being affected by the style.
   * @return the custom display name
   */
  public String createDisplayName(String entityName) {
    return Perx.getInstance().getGroupHandler().styleExecutor().createDisplayName(this, entityName);
  }

  /**
   * Converts this group into a new database model representation.
   *
   * @return the newly allocated group model
   */
  @Override
  public GroupModel toModel() {
    model.setPermissions(permissionRepository.toPermissionMap());
    model.setPrefix(getStyle(GroupStyleKey.PREFIX));
    model.setSuffix(getStyle(GroupStyleKey.SUFFIX));
    return model;
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

  /**
   * Updates the style at {@code key} to given {@code value}.
   * <p>If {@code value} is null or empty, the style is being removed from this group.
   *
   * @param key   the key to assign the style to
   * @param value the value, being the actual style, whereas null or an empty string represent
   *              nothingness, such that the style is removed
   * @return the potentially previously associated value with {@code key}
   */
  @CanIgnoreReturnValue
  public @Nullable String setStyle(GroupStyleKey key, @Nullable String value) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return (StringUtils.isNotEmpty(value)
        ? styles.put(key, ChatColor.translateAlternateColorCodes('&', value))
        : styles.remove(key));
  }

  public @Nullable String getStyle(GroupStyleKey key) {
    Preconditions.checkNotNull(key, "Key must not be null");
    return styles.get(key);
  }

  /**
   * Returns true if this group has any style applied to it at all.
   *
   * @return true if this group has any (custom) styling
   */
  public boolean hasStyle() {
    return Arrays.stream(GroupStyleKey.values()).anyMatch(this::hasStyle);
  }

  /**
   * Returns true if this group has a custom style for {@code key}.
   * <p>True is returned, if the style at given {@code key} is neither null nor empty.
   *
   * @param key the target style to test
   * @return true if this group has a style applied at {@code key}
   */
  public boolean hasStyle(GroupStyleKey key) {
    return StringUtils.isNotEmpty(styles.get(key));
  }

  public PerxPermissionRepository getPermissionRepository() {
    return permissionRepository;
  }

  /**
   * Returns the priority of this group (natural order).
   * <p>The lower the returning value, the more important this group becomes.
   *
   * @return the priority of this group, lower meaning more important
   */
  public int getPriority() {
    return model.getPriority();
  }

  /**
   * Updates the priority of this group to given {@code priority}.
   * <p>The lower the priority, the more important this group becomes.
   *
   * @param priority the new priority of this group
   * @apiNote Note that previously allocated group models will not have this applied automatically.
   */
  public void setPriority(int priority) {
    model.setPriority(priority);
  }

  public boolean isDefault() {
    return model.isDefault();
  }

  public void setDefault(boolean isDefault) {
    model.setDefault(isDefault);
  }

  @Override
  public String toString() {
    return "PerxGroup{" +
        "name='" + getName() + '\'' +
        ", styles=" + styles +
        ", permissions=" + permissionRepository +
        ", model=" + model +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PerxGroup group = (PerxGroup) o;
    return Objects.equals(permissionRepository, group.permissionRepository)
        && Objects.equals(model, group.model);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionRepository, model);
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
    Iterator<PerxUser> iterator = Perx.getInstance().getUserService().getRepository().iterator();
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
