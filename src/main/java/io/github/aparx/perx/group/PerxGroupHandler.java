package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.intersection.PerxUserGroup;
import io.github.aparx.perx.group.intersection.PerxUserGroupService;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.permission.PerxPermissionRepository;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.user.PerxUserService;
import io.github.aparx.perx.utils.BukkitThreads;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * A handling class for subscribing, unsubscribing and generally handling groups.
 * <p>This handler has the responsibility of also updating the database.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 04:34
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public record PerxGroupHandler(
    Database database,
    GroupStyleExecutor styleExecutor,
    PerxUserService userService,
    PerxGroupService groupService,
    PerxUserGroupService userGroupService) {

  public PerxGroupHandler {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(styleExecutor, "Style executor must not be null");
    Preconditions.checkNotNull(userService, "User service must not be null");
    Preconditions.checkNotNull(groupService, "Group service must not be null");
    Preconditions.checkNotNull(userGroupService, "User group service must not be null");
  }

  /**
   * Subscribes {@code user} to all {@code groups} (if not already subscribed to), but only
   * within the cache, such that a refetch of the user's groups may not include these groups.
   *
   * @param user   the subscriber
   * @param groups the groups to which the user subscribes to (in cache)
   */
  public void subscribeInCache(PerxUser user, Collection<PerxGroup> groups) {
    groups.stream().filter((group) -> !user.hasGroup(group.getName()))
        .map((group) -> PerxUserGroup.of(user.getId(), group))
        .forEach((userGroup) -> doSubscribeInCache(user, userGroup));
  }

  /**
   * Calls {@code unsubscribe} with given user group if it is either invalid or expired.
   *
   * @param userGroup the group to test and potentially unsubscribe
   */
  public void unsubscribeIfNeeded(PerxUserGroup userGroup) {
    if (!userGroup.isMarkedRemoved() && (!userGroup.isGroupValid() || userGroup.isExpired()))
      unsubscribe(userGroup);
  }

  /**
   * Applies the (optional) style and permissions of given user group to its user, if the user is
   * currently online. This is ensured to happen on the primary thread.
   * <p>If given user group either became invalid or expired, the group is automatically
   * unsubscribed asynchronously and the group marked as removed, thus hindering the application
   * of the styles and permissions.
   *
   * @param userGroup the user group to be applied to its player
   */
  public void applyUserGroupToPlayer(PerxUserGroup userGroup) {
    unsubscribeIfNeeded(userGroup);
    if (!userGroup.isMarkedRemoved())
      BukkitThreads.runOnPrimaryThread(() -> {
        @Nullable PerxGroup group = userGroup.findGroup();
        @Nullable Player player = Bukkit.getPlayer(userGroup.getUserId());
        if (!userGroup.isMarkedRemoved() && player != null && group != null)
          applyGroupToPlayer(player, group);
      });
  }

  /**
   * Completely reinitializes {@code player}, such that styles, permissions and more are unset
   * and then reset, depending on the player's subscribed (user-) groups.
   *
   * @param player the player to reinitialize
   * @return a void returning promise, indicating when the reinitialization is done
   */
  @CanIgnoreReturnValue
  public CompletableFuture<Void> reinitializePlayer(Player player) {
    Collection<PerxGroup> defaults = groupService.getDefaults();
    @Nullable PerxUser cached = Perx.getInstance().getUserService().getRepository().get(player);
    styleExecutor.resetAll(player);
    if (cached != null)
      // remove all only in cache living groups
      cached.getSubscribed().stream()
          .filter(Predicate.not(PerxUserGroup::isModelInDatabase))
          .toList()
          .forEach(cached::removeGroup);
    return Perx.getInstance().getUserService()
        .getOrFetch(player.getUniqueId(), UserCacheStrategy.AUTO)
        .thenAccept((user) -> {
          subscribeInCache(user, defaults);
          updateUser(user);
        });
  }

  /**
   * Reinitializes all online players on the primary thread.
   *
   * @see #reinitializePlayer(Player)
   */
  public void reinitializeAllPlayers() {
    BukkitThreads.runOnPrimaryThread(() -> {
      Bukkit.getOnlinePlayers().forEach(this::reinitializePlayer);
    });
  }

  /**
   * Updates the styles, permissions of all of {@code user}'s groups for given user and
   * unsubscribes if a group should be removed (for example due to time limitation).
   *
   * @param user the user to update
   */
  public void updateUser(PerxUser user) {
    user.getSubscribed().stream().sorted().forEach(this::applyUserGroupToPlayer);
  }

  /**
   * Applies {@code group}'s permissions to {@code player} and the style, if either player has no
   * user within cache or if {@code group} is the highest group the player is subscribed to.
   *
   * @param player the player to apply the group to
   * @param group  the group that is supposed to be applied to {@code player}
   */
  public void applyGroupToPlayer(Player player, PerxGroup group) {
    BukkitThreads.runOnPrimaryThread(() -> {
      if (!player.isOnline()) return;
      PerxPermissionRepository register = group.getPermissionRepository();
      register.getAdapter().clearPermissions(player);
      register.forEach((perm) -> perm.apply(player));
      @Nullable PerxUser user = userService.getRepository().get(player);
      if (user == null && group.hasStyle())
        styleExecutor.apply(group, player);
      else if (user != null)
        applyHighestPossibleStyle(user);
    });
  }

  /**
   * Removes all styles, permissions and more from {@code group} for {@code player}.
   * <p>This is ensured to happen on the primary thread.
   *
   * @param player the target player to be affected by the reset
   * @param group  the group that is supposed to be reset for {@code player}
   */
  public void resetGroupFromPlayer(Player player, PerxGroup group) {
    BukkitThreads.runOnPrimaryThread(() -> {
      if (!player.isOnline()) return;
      group.getPermissionRepository().getAdapter().clearPermissions(player);
      styleExecutor.reset(group, player);
      @Nullable PerxUser user = userService.getRepository().get(player);
      if (user != null)
        // Temporary fix for: https://github.com/aparx/perx/issues/2
        applyHighestPossibleStyle(user);
    });
  }

  /**
   * Applies the highest possible style for {@code user} of groups they are subscribed to.
   * <p>The highest possible style is the style of any group that contains any custom styling,
   * whose priority is the lowest (thus has highest meaning).
   * <p>For that, the {@code user} is providing all their subscribed to groups, from which the
   * highest is filtered out and directly applied.
   *
   * @param user the user to apply the highest possible style to
   */
  public void applyHighestPossibleStyle(PerxUser user) {
    @Nullable Player player = user.getPlayer();
    Preconditions.checkNotNull(player, "Player is now offline");
    user.getHighestStyledGroup().ifPresent((userGroup) -> {
      @Nullable PerxGroup group = userGroup.findGroup();
      if (group != null) styleExecutor.apply(group, player);
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group) {
    return groupService.upsert(group).thenApply((res) -> {
      if (res.getNumLinesChanged() != 0) reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> create(PerxGroup group) {
    return groupService.create(group).thenApply((res) -> {
      if (res) reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> delete(PerxGroup group) {
    return groupService.delete(group.getName()).thenApply((res) -> {
      // force unsubscribe in cache, even if the database interaction failed
      group.forEach((user) -> doUnsubscribeInCache(user, group));
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Integer> update(PerxGroup group) {
    return groupService.update(group).thenApply((res) -> {
      if (res > 0) Perx.getInstance().getGroupHandler().reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(PerxGroup group) {
    return userGroupService.deleteByGroup(group.getName()).thenApply((res) -> {
      // force unsubscribe in cache, even if the database interaction failed
      group.forEach((user) -> doUnsubscribeInCache(user, group));
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(PerxUserGroup userGroup) {
    userGroup.markRemoved();
    return fetchUserToPerform(userGroup.getUserId(), (user) ->
        (userGroup.isModelInDatabase()
            ? database.executeAsync(() -> userGroupService.getDao().deleteById(userGroup.getId()))
            : CompletableFuture.completedFuture(1)
        ).thenApply((result) -> {
          // force unsubscribe in cache, even if the database interaction failed
          doUnsubscribeInCache(user, userGroup.getGroupName(), userGroup.findGroup());
          return result < 1;
        }));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(UUID userId, String groupName) {
    return unsubscribe(userId, groupService.getRepository().getLoudly(groupName));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(UUID userId, PerxGroup group) {
    return fetchUserToPerform(userId, (user) -> database
        .executeAsync(() -> {
          DeleteBuilder<UserGroupModel, Long> deleteBuilder =
              userGroupService.getDao().deleteBuilder();
          deleteBuilder.limit(1L).where()
              // unique combo index should force O(1) lookup in database engine
              .eq(UserGroupModel.USER_ID_FIELD_NAME, user.getId()).and()
              .eq(UserGroupModel.GROUP_ID_FIELD_NAME, group.getName());
          return deleteBuilder.delete();
        })
        .thenApply((res) -> {
          // force unsubscribe in cache, even if the database interaction failed
          doUnsubscribeInCache(user, group.getName(), group);
          return res != 0;
        }));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> subscribe(UUID userId, String groupName) {
    return subscribe(userId, groupName, null);
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> subscribe(UUID userId, String groupName, @Nullable Date end) {
    PerxGroupRepository groupRepository = groupService.getRepository();
    PerxUserGroup userGroup = PerxUserGroup.of(userId, groupRepository.getLoudly(groupName));
    if (end != null) userGroup.setEndingDate(end);
    return subscribe(userGroup);
  }

  private CompletableFuture<Boolean> subscribe(PerxUserGroup userGroup) {
    PerxGroupRepository groupRepository = groupService.getRepository();
    PerxGroup group = userGroup.getGroup();
    Preconditions.checkState(groupRepository.contains(group), "Group must be registered");
    UserGroupModel temporaryModel = new UserGroupModel(
        userGroup.getUserId(), new GroupModel(group.getName()),
        userGroup.getEndingDate());
    return fetchUserToPerform(userGroup.getUserId(), (user) -> database
        .executeAsync(() -> userGroupService.getDao().create(temporaryModel))
        .thenApply((x) -> {
          if (x == 0)
            // subscription was not successful
            return false;
          userGroup.setId(temporaryModel.getId());
          doSubscribeInCache(user, userGroup);
          Perx.getLogger().log(Level.FINE, () -> String.format(
              "(UserGroup-%s) User %s subscribes to %s",
              userGroup.getId(), userGroup.getUserId(), userGroup.getGroupName()));
          return true;
        }));
  }

  private CompletableFuture<Boolean> fetchUserToPerform(
      UUID userId, Function<PerxUser, CompletableFuture<Boolean>> action) {
    return Perx.getInstance().getUserService()
        .getOrFetch(userId, UserCacheStrategy.AUTO)
        .thenCompose(action);
  }

  private void doUnsubscribeInCache(PerxUser user, PerxGroup group) {
    doUnsubscribeInCache(user, group.getName(), group);
  }

  private void doUnsubscribeInCache(PerxUser user, String groupName, @Nullable PerxGroup group) {
    BukkitThreads.runOnPrimaryThread(() -> {
      user.removeGroup(groupName);
      userGroupService.getRepository().removeByGroup(groupName);
      @Nullable Player player = user.getPlayer();
      if (group != null && player != null)
        resetGroupFromPlayer(player, group);
    });
  }

  private void doSubscribeInCache(PerxUser user, PerxUserGroup userGroup) {
    BukkitThreads.runOnPrimaryThread(() -> {
      user.addGroup(userGroup);
      userGroupService.getRepository().put(userGroup);
      @Nullable Player player = user.getPlayer();
      @Nullable PerxGroup group = userGroup.findGroup();
      if (player != null && group != null)
        applyGroupToPlayer(player, group);
    });
  }

}
