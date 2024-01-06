package io.github.aparx.perx;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.config.ConfigManager;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.group.PerxGroupUpdateTask;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.group.union.controller.PerxUserGroupManager;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.controller.PerxGroupController;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.controller.PerxGroupManager;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.listeners.DefaultListener;
import io.github.aparx.perx.message.MessageMap;
import io.github.aparx.perx.message.MessageRegister;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserManager;
import io.github.aparx.perx.user.controller.PerxUserController;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.HashSet;
import java.util.List;
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

  private final List<Listener> listeners = List.of(new DefaultListener());
  private final MessageRegister messages = new MessageMap();

  private volatile boolean loaded;

  private @Nullable Plugin plugin;
  private @Nullable PerxUserController userController;
  private @Nullable PerxGroupController groupController;
  private @Nullable PerxGroupHandler groupHandler;
  private @Nullable Database database;
  private @Nullable PerxUserGroupManager userGroupController;
  private @Nullable PerxGroupUpdateTask groupUpdateTask;
  private @Nullable ConfigManager configManager;
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

  public PerxUserGroupManager getUserGroupController() {
    return require(userGroupController, "UserGroupController is undefined");
  }

  public PerxUserController getUserController() {
    return require(userController, "UserRegister is undefined");
  }

  public PerxGroupUpdateTask getGroupUpdateTask() {
    return require(groupUpdateTask, "GroupUpdateTask is undefined");
  }

  public ConfigManager getConfigManager() {
    return require(configManager, "ConfigManager is undefined");
  }

  public MessageRegister getMessages() {
    return messages;
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
        this.database = database;
        listeners.forEach((x) -> Bukkit.getPluginManager().registerEvents(x, plugin));
        (this.configManager = new ConfigManager(plugin.getDataFolder())).load();
        (this.groupController = new PerxGroupManager(database)).load();
        (this.userGroupController = new PerxUserGroupManager(database)).load();
        this.userController = new PerxUserManager(database, userGroupController);
        this.groupHandler = new PerxGroupHandler(database, styleExecutor);
        (this.groupUpdateTask = new PerxGroupUpdateTask(plugin)).start();
        return (this.loaded = true);
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
        if (groupUpdateTask != null)
          groupUpdateTask.stop();
        if (groupHandler != null && userController != null)
          Bukkit.getOnlinePlayers().forEach((player) -> {
            @Nullable PerxUser user = userController.get(player);
            if (user == null) return;
            // clear player from all groups without unsubscribing
            for (PerxUserGroup userGroup : new HashSet<>(user.getSubscribed())) {
              @Nullable PerxGroup group = userGroup.findGroup();
              if (group != null) groupHandler.resetGroupFromPlayer(player, group);
            }
          });
        this.plugin = null;
      } finally {
        loaded = false;
        listeners.forEach(HandlerList::unregisterAll);
      }
      return true;
    }
  }

}
