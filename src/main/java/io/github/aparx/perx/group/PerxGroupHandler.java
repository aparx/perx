package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.j256.ormlite.dao.Dao;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.controller.PerxGroupController;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.group.union.controller.PerxUserGroupController;
import io.github.aparx.perx.group.union.controller.PerxUserGroupManager;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.permission.PerxPermissionRegister;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.user.controller.PerxUserController;
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
public record PerxGroupHandler(Database database, GroupStyleExecutor styleExecutor) {

  public PerxGroupHandler {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(styleExecutor, "Style executor must not be null");
  }

  /**
   * Subscribes {@code user} to all {@code groups} (if not already subscribed to), but only
   * within the cache, such that a refetch of the user's groups may not include these groups.
   *
   * @param user   the subscriber
   * @param groups the groups to which the user subscribes to (in cache)
   */
  public void subscribeInCache(PerxUser user, Collection<PerxGroup> groups) {
    PerxUserGroupManager controller = Perx.getInstance().getUserGroupController();
    groups.stream().filter((group) -> !user.hasGroup(group.getName()))
        .map((group) -> PerxUserGroup.of(user.getId(), group))
        .forEach((userGroup) -> doSubscribeInCache(controller, user, userGroup));
  }

  public void unsubscribeIfPastEndDate(PerxUserGroup userGroup) {
    if (userGroup.isMarkedRemoved()) return;
    @Nullable Date end = userGroup.getEndingDate();
    if (!userGroup.isGroupValid() || end != null && System.currentTimeMillis() > end.getTime())
      unsubscribe(userGroup);
  }

  public void applyUserGroupToPlayer(PerxUserGroup userGroup) {
    unsubscribeIfPastEndDate(userGroup);
    @Nullable PerxGroup group = userGroup.findGroup();
    @Nullable Player player = Bukkit.getPlayer(userGroup.getUserId());
    if (!userGroup.isMarkedRemoved() && player != null && group != null)
      applyGroupToPlayer(player, group);
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Void> reinitializePlayer(Player player) {
    Collection<PerxGroup> defaults = Perx.getInstance().getGroupController().getDefaults();
    @Nullable PerxUser cached = Perx.getInstance().getUserController().get(player);
    styleExecutor.resetAll(player);
    if (cached != null)
      // remove all only in cache living groups
      cached.getSubscribed().stream()
          .filter(Predicate.not(PerxUserGroup::isModelInDatabase))
          .forEach(cached::removeGroup);
    return Perx.getInstance().getUserController()
        .fetchOrGet(player.getUniqueId(), UserCacheStrategy.AUTO)
        .thenAccept((user) -> {
          subscribeInCache(user, defaults);
          updateUser(user);
        });
  }

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
      PerxPermissionRegister register = group.getPermissions();
      register.getAdapter().clearPermissions(player);
      register.forEach((perm) -> perm.apply(player, true));
      PerxUserController userController = Perx.getInstance().getUserController();
      @Nullable PerxUser user = userController.get(player);
      if (user == null && group.hasStyle())
        styleExecutor.apply(group, player);
      else if (user != null)
        applyHighestPossibleStyle(user);
    });
  }

  public void resetGroupFromPlayer(Player player, PerxGroup group) {
    BukkitThreads.runOnPrimaryThread(() -> {
      if (!player.isOnline()) return;
      group.getPermissions().getAdapter().clearPermissions(player);
      styleExecutor.reset(group, player);
      @Nullable PerxUser user = Perx.getInstance().getUserController().get(player);
      if (user != null)
        // Fix for https://github.com/aparx/perx/issues/2
        applyHighestPossibleStyle(user);
    });
  }

  public void applyHighestPossibleStyle(PerxUser user) {
    @Nullable Player player = user.getPlayer();
    Preconditions.checkNotNull(player, "Player is now offline");
    user.getHighestUserGroup((group) -> group.isGroupValid() && group.getGroup().hasStyle())
        .ifPresent((userGroup) -> {
          @Nullable PerxGroup group = userGroup.findGroup();
          if (group != null) styleExecutor.apply(group, player);
        });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Dao.CreateOrUpdateStatus> upsert(PerxGroup group) {
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    return groupController.upsert(group).thenApply((res) -> {
      reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> create(PerxGroup group) {
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    return groupController.create(group).thenApply((res) -> {
      reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> delete(PerxGroup group) {
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    return groupController.delete(group.getName()).thenApply((res) -> {
      group.updatePlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Integer> update(PerxGroup group) {
    return Perx.getInstance().getGroupController().update(group).thenApply((res) -> {
      Perx.getInstance().getGroupHandler().reinitializeAllPlayers();
      return res;
    });
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(PerxUserGroup userGroup) {
    userGroup.markRemoved();
    PerxUserGroupManager controller = Perx.getInstance().getUserGroupController();
    return fetchUserToPerform(userGroup.getUserId(), (user) ->
        (userGroup.isModelInDatabase()
            ? database.executeAsync(() -> controller.getDao().deleteById(userGroup.getId()))
            : CompletableFuture.completedFuture(1)
        ).thenApply((result) -> {
          if (result < 1) return false;
          doUnsubscribeInCache(controller, user, userGroup.getGroupName(), userGroup.findGroup());
          return true;
        }));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(UUID userId, String groupName) {
    return Perx.getInstance().getUserGroupController()
        .find(userId, groupName)
        .map(this::unsubscribe)
        .orElseGet(() -> CompletableFuture.completedFuture(false));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(UUID userId, PerxGroup group) {
    PerxUserGroupManager userGroupController = Perx.getInstance().getUserGroupController();
    return fetchUserToPerform(userId, (user) -> database
        .executeAsync(() -> userGroupController.getDao()
            .deleteBuilder().limit(1L).where()
            // unique combo index should force O(1) in engine's register
            .eq(UserGroupModel.USER_ID_FIELD_NAME, user)
            .eq(UserGroupModel.GROUP_ID_FIELD_NAME, group.getName())
            .query())
        .thenApply((result) -> {
          // we force the unsubscribe in cache anyway (for default groups in cache)
          doUnsubscribeInCache(userGroupController, user, group.getName(), group);
          return !result.isEmpty();
        }));
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> subscribe(UUID userId, String groupName) {
    return subscribe(userId, groupName, null);
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> subscribe(UUID userId, String groupName, @Nullable Date end) {
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    PerxUserGroup userGroup = PerxUserGroup.of(userId, groupController.getLoudly(groupName));
    if (end != null) userGroup.setEndingDate(end);
    return subscribe(userGroup);
  }

  private CompletableFuture<Boolean> subscribe(PerxUserGroup userGroup) {
    PerxGroup group = userGroup.getGroup();
    Preconditions.checkState(Perx.getInstance().getGroupController().contains(group),
        "Group must be registered");
    PerxUserGroupManager userGroupController = Perx.getInstance().getUserGroupController();
    UserGroupModel temporaryModel = new UserGroupModel(
        userGroup.getUserId(), new GroupModel(group.getName()),
        userGroup.getEndingDate());
    return fetchUserToPerform(userGroup.getUserId(), (user) -> database
        .executeAsync(() -> userGroupController.getDao().create(temporaryModel))
        .thenApply((x) -> {
          if (x == 0)
            // subscription was not successful
            return false;
          userGroup.setId(temporaryModel.getId());
          doSubscribeInCache(userGroupController, user, userGroup);
          Perx.getLogger().log(Level.FINE, () -> String.format(
              "(UserGroup-%s) User %s subscribes to %s",
              userGroup.getId(), userGroup.getUserId(), userGroup.getGroupName()));
          return true;
        }));
  }

  private CompletableFuture<Boolean> fetchUserToPerform(
      UUID userId, Function<PerxUser, CompletableFuture<Boolean>> action) {
    return Perx.getInstance().getUserController()
        .fetchOrGet(userId, UserCacheStrategy.AUTO)
        .thenCompose(action);
  }

  private void doUnsubscribeInCache(
      PerxUserGroupController userGroupController,
      PerxUser user,
      String groupName,
      @Nullable PerxGroup group) {
    user.removeGroup(groupName);
    userGroupController.removeByGroup(groupName);
    @Nullable Player player = user.getPlayer();
    if (group != null && player != null)
      resetGroupFromPlayer(player, group);
  }

  private void doSubscribeInCache(
      PerxUserGroupController userGroupController, PerxUser user, PerxUserGroup userGroup) {
    user.addGroup(userGroup);
    userGroupController.register(userGroup);
    @Nullable Player player = user.getPlayer();
    @Nullable PerxGroup group = userGroup.findGroup();
    if (player != null && group != null)
      applyGroupToPlayer(player, group);
  }

}
