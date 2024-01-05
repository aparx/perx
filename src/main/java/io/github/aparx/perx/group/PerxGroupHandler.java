package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroupModel;
import io.github.aparx.perx.group.many.PerxUserGroup;
import io.github.aparx.perx.group.many.PerxUserGroupController;
import io.github.aparx.perx.group.many.PerxUserGroupManager;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.permission.PermissionRegister;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

  public void updateUserGroup(PerxUserGroup userGroup) {
    updateValidity(userGroup);
    @Nullable PerxGroup group = userGroup.findGroup();
    @Nullable Player player = Bukkit.getPlayer(userGroup.getUserId());
    if (!userGroup.isMarkedRemoved() && player != null && group != null)
      applyGroupSync(player, group);
  }

  public void updateValidity(PerxUserGroup userGroup) {
    if (userGroup.isMarkedRemoved()) return;
    @Nullable Date end = userGroup.getEndingDate();
    Bukkit.broadcastMessage("§4CHECK VALIDITY: " + end);
    if (!userGroup.isGroupValid() || end != null && System.currentTimeMillis() > end.getTime()) {
      unsubscribe(userGroup);
      Bukkit.broadcastMessage("§cAutomatically unsubscribe: " + userGroup.getGroupName());
    }
  }

  /**
   * Applies {@code group}'s permissions and style to {@code player}.
   *
   * @param player the player to apply the group to
   * @param group  the group that is supposed to be applied to {@code player}
   */
  public void applyGroupSync(Player player, PerxGroup group) {
    if (!Bukkit.isPrimaryThread())
      Bukkit.getScheduler().runTask(Perx.getPlugin(), () -> applyGroup(player, group));
    else applyGroup(player, group);
  }

  /** @deprecated Recommend {@code applyGroupSync} instead */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  public void applyGroup(Player player, PerxGroup group) {
    PermissionRegister register = group.getPermissions();
    Bukkit.getScheduler().runTask(Perx.getPlugin(), () -> {
      register.getAdapter().clearPermissions(player);
      register.forEach((perm) -> perm.apply(player, true));
      // TODO check for priority when applying styles|
      styleExecutor.apply(group, player);
    });
  }

  public void resetGroup(Player player, PerxGroup group) {
    group.getPermissions().getAdapter().clearPermissions(player);
    styleExecutor.remove(group, player);
  }

  @CanIgnoreReturnValue
  public CompletableFuture<Boolean> unsubscribe(PerxUserGroup userGroup) {
    userGroup.markRemoved();
    PerxUserGroupManager controller = Perx.getInstance().getUserGroupController();
    return fetchUserToPerform(userGroup.getUserId(), (user) -> database
        .executeAsync(() -> controller.getDao().deleteById(userGroup.getId()))
        .thenApply((result) -> {
          if (result < 1) {
            userGroup.deleteRemovalMark();
            return false;
          }
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
          if (result.isEmpty()) return false;
          doUnsubscribeInCache(userGroupController, user, group.getName(), group);
          return true;
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
    return fetchUserToPerform(userGroup.getUserId(), (user) -> database
        .executeAsync(() -> userGroupController.getDao()
            .create(new UserGroupModel(
                userGroup.getUserId(),
                new GroupModel(group.getName()),
                userGroup.getEndingDate())
            ))
        .thenApply((x) -> {
          if (x == 0)
            // subscription was not successful
            return false;
          user.addGroup(userGroup);
          userGroupController.register(userGroup);
          @Nullable Player player = user.getPlayer();
          if (player != null) applyGroupSync(player, group);
          return true;
        }));
  }

  private CompletableFuture<Boolean> fetchUserToPerform(
      UUID userId, Function<PerxUser, CompletableFuture<Boolean>> action) {
    return Perx.getInstance().getUserController()
        .fetch(userId, UserCacheStrategy.AUTO)
        .thenCompose((user) -> {
          if (user == null)
            return CompletableFuture.completedFuture(false);
          return action.apply(user);
        });
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
      resetGroup(player, group);
  }

}
