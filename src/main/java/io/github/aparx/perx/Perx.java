package io.github.aparx.perx;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.data.many.UserGroupController;
import io.github.aparx.perx.group.PerxGroupController;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.PerxGroupManager;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserManager;
import io.github.aparx.perx.user.PerxUserController;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:24
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class Perx {

  private static final Perx instance = new Perx();

  private transient final Object lock = new Object();

  private volatile boolean loaded;

  private @Nullable Plugin plugin;
  private @Nullable PerxUserController userController;
  private @Nullable PerxGroupController groupController;
  private @Nullable PerxGroupHandler groupHandler;
  private @Nullable Database database;
  private @Nullable UserGroupController userGroupController;
  private Logger logger = Bukkit.getLogger();

  private Perx() {}

  public static Perx getInstance() {
    return instance;
  }

  public static Plugin getPlugin() {
    @Nullable Plugin plugin = instance.plugin;
    Preconditions.checkState(plugin != null, "Perx is not loaded");
    return plugin;
  }

  public static Logger getLogger() {
    return instance.logger;
  }

  private static <T> T require(@Nullable T val, String message) {
    if (val == null)
      throw new IllegalArgumentException(message);
    return val;
  }

  public Database getDatabase() {
    return require(database, "Database is undefined");
  }

  public PerxGroupController getGroupController() {
    return require(groupController, "GroupController is undefined");
  }

  public PerxGroupHandler getGroupHandler() {
    return require(groupHandler, "GroupHandler is undefined");
  }

  public UserGroupController getUserGroupController() {
    return require(userGroupController, "UserGroupController is undefined");
  }

  public PerxUserController getUserController() {
    return require(userController, "UserRegister is undefined");
  }

  public boolean isLoaded() {
    return loaded;
  }

  @CanIgnoreReturnValue
  public boolean load(Plugin plugin, Database database, GroupStyleExecutor styleExecutor) {
    Preconditions.checkNotNull(plugin, "Plugin must not be null");
    if (loaded) return false;
    synchronized (lock) {
      if (loaded) return false;
      try {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.loaded = true;
        this.database = database;
        (this.groupController = new PerxGroupManager(database)).load();
        (this.userGroupController = new UserGroupController(database, groupController)).load();
        this.userController = new PerxUserManager(database, userGroupController);
        this.groupHandler = new PerxGroupHandler(
            database, userController, groupController, userGroupController, styleExecutor);
        return true;
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Severe error on load", e);
        Bukkit.getPluginManager().disablePlugin(plugin);
        return false;
      }
    }
  }

  @CanIgnoreReturnValue
  public boolean unload() {
    if (!loaded) return false;
    synchronized (lock) {
      if (!loaded) return false;
      try {
        this.plugin = null;
        if (groupController != null && userController != null)
          Bukkit.getOnlinePlayers().forEach((player) -> {
            @Nullable PerxUser user = userController.get(player);
            if (user == null) return;
            // clear player from all groups without unsubscribing
            //for (PerxGroup group : new HashSet<>(user.getSubscribed()))
            //  groupHandler.clearSubscriber(player, group);
          });
      } finally {
        loaded = false;
      }
      return true;
    }
  }

}
