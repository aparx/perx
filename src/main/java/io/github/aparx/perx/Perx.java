package io.github.aparx.perx;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.config.ConfigManager;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.group.PerxGroupUpdateTask;
import io.github.aparx.perx.group.intersection.PerxUserGroup;
import io.github.aparx.perx.group.intersection.PerxUserGroupManager;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupService;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.PerxGroupManager;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.group.intersection.PerxUserGroupService;
import io.github.aparx.perx.listeners.DefaultListener;
import io.github.aparx.perx.message.MessageMap;
import io.github.aparx.perx.message.MessageRepository;
import io.github.aparx.perx.sign.PerxSignManager;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserManager;
import io.github.aparx.perx.user.PerxUserService;
import org.bukkit.Bukkit;
import org.bukkit.Server;
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
  private final MessageRepository messages = new MessageMap();

  private volatile boolean loaded;

  private @Nullable Plugin plugin;
  private @Nullable PerxUserService userService;
  private @Nullable PerxGroupService groupService;
  private @Nullable PerxGroupHandler groupHandler;
  private @Nullable Database database;
  private @Nullable PerxUserGroupService userGroupService;
  private @Nullable PerxGroupUpdateTask groupUpdateTask;
  private @Nullable ConfigManager configManager;
  private @Nullable PerxSignManager signManager;
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

  public PerxGroupService getGroupService() {
    return require(groupService, "Service is undefined");
  }

  public PerxGroupHandler getGroupHandler() {
    return require(groupHandler, "Service is undefined");
  }

  public PerxUserGroupService getUserGroupService() {
    return require(userGroupService, "Service is undefined");
  }

  public PerxUserService getUserService() {
    return require(userService, "Service is undefined");
  }

  public PerxGroupUpdateTask getGroupUpdateTask() {
    return require(groupUpdateTask, "GroupUpdateTask is undefined");
  }

  public ConfigManager getConfigManager() {
    return require(configManager, "ConfigManager is undefined");
  }

  public PerxSignManager getSignManager() {
    return require(signManager, "SignManager is undefined");
  }

  public MessageRepository getMessages() {
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
        (this.groupService = new PerxGroupManager(database)).load();
        (this.userGroupService = new PerxUserGroupManager(database)).load();
        this.userService = new PerxUserManager(database, userGroupService);
        this.groupHandler = new PerxGroupHandler(database, styleExecutor);
        (this.groupUpdateTask = new PerxGroupUpdateTask(plugin)).start();
        (this.signManager = new PerxSignManager(plugin.getDataFolder())).load();
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
        if (groupHandler != null && userService != null)
          Bukkit.getOnlinePlayers().forEach((player) -> {
            @Nullable PerxUser user = userService.get(player);
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
