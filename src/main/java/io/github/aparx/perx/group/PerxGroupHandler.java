package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.group.GroupModel;
import io.github.aparx.perx.database.data.many.UserGroup;
import io.github.aparx.perx.database.data.many.UserGroupController;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.permission.PermissionRegister;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserController;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
public final class PerxGroupHandler {

  private final Database database;
  private final PerxGroupController groupController;
  private final UserGroupController userGroupController;
  private final PerxUserController userController;
  private final GroupStyleExecutor styleExecutor;

  public PerxGroupHandler(
      Database database,
      PerxUserController userController,
      PerxGroupController groupController,
      UserGroupController userGroupController,
      GroupStyleExecutor styleExecutor) {
    Preconditions.checkNotNull(database, "Database must not be null");
    Preconditions.checkNotNull(userController, "User controller must not be null");
    Preconditions.checkNotNull(groupController, "Group controller must not be null");
    Preconditions.checkNotNull(userGroupController, "User group controller must not be null");
    Preconditions.checkNotNull(styleExecutor, "Style executor must not be null");
    this.database = database;
    this.userController = userController;
    this.groupController = groupController;
    this.userGroupController = userGroupController;
    this.styleExecutor = styleExecutor;
  }

  public Database getDatabase() {
    return database;
  }

  public PerxUserController getUserController() {
    return userController;
  }

  public PerxGroupController getGroupController() {
    return groupController;
  }

  public UserGroupController getUserGroupController() {
    return userGroupController;
  }

  public GroupStyleExecutor getStyleExecutor() {
    return styleExecutor;
  }

  /**
   * Applies {@code group}'s permissions and style to {@code player}.
   *
   * @param player the player to apply the group to
   * @param group  the group that is supposed to be applied to {@code player}
   */
  public void applyGroup(Player player, PerxGroup group) {
    PermissionRegister register = group.getPermissions();
    register.getAdapter().clearPermissions(player);
    register.forEach((x) -> x.apply(player, true));
    // TODO check for priority when applying styles|
    styleExecutor.apply(group, player);
  }

  public void resetGroup(Player player, PerxGroup group) {
    group.getPermissions().getAdapter().clearPermissions(player);
    styleExecutor.remove(group, player);
  }

  public CompletableFuture<Boolean> unsubscribe(UUID uuid, PerxGroup group) {
    return userController.fetch(uuid, UserCacheStrategy.AUTO).thenCompose((user) -> {
      if (user == null || !user.getSubscribed().contains(group))
        return CompletableFuture.completedFuture(false);
      return database.executeAsync(() -> userGroupController.getDao().deleteBuilder()
          .limit(1L).where()
          .eq(UserGroup.USER_ID_FIELD_NAME, user)
          .eq(UserGroup.GROUP_ID_FIELD_NAME, group)
          .query()
      ).thenApply((x) -> {
        if (x.isEmpty()) return false;
        user.getSubscribed().remove(group);
        @Nullable Player player = user.getPlayer();
        if (player != null) resetGroup(player, group);
        return true;
      });
    });
  }

  public CompletableFuture<Boolean> subscribe(UUID uuid, PerxGroup group) {
    Preconditions.checkState(groupController.contains(group), "Group must be registered");
    return userController.fetch(uuid, UserCacheStrategy.AUTO).thenCompose((user) -> {
      if (user == null || user.getSubscribed().contains(group))
        return CompletableFuture.completedFuture(false);
      return database.executeAsync(() -> userGroupController.getDao()
          .create(new UserGroup(uuid, new GroupModel(group.getName())))
      ).thenApply((x) -> {
        if (x == 0)
          // subscription was not successful
          return false;
        user.getSubscribed().add(group);
        @Nullable Player player = user.getPlayer();
        if (player != null) applyGroup(player, group);
        return true;
      });
    });
  }

  public CompletableFuture<Boolean> subscribe(OfflinePlayer player, PerxGroup group) {
    return subscribe(player.getUniqueId(), group);
  }
}
