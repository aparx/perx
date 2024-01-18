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
import io.github.aparx.perx.permission.PermissionAdapterFactory;
import io.github.aparx.perx.sign.PerxSignFile;
import io.github.aparx.perx.sign.PerxSignHandler;
import io.github.aparx.perx.sign.PerxSignStorage;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserManager;
import io.github.aparx.perx.user.PerxUserService;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:24
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class Perx {

  private static final Object lock = new Object();

  private static @Nullable Perx instance;

  private final List<Listener> listeners = List.of(new DefaultListener());
  private final MessageRepository messages = new MessageMap();

  private volatile boolean loaded;

  private @Nullable Plugin plugin;
  private @Nullable PermissionAdapterFactory permissionAdapterFactory;

  private @Nullable PerxUserService userService;
  private @Nullable PerxGroupService groupService;
  private @Nullable PerxGroupHandler groupHandler;
  private @Nullable Database database;
  private @Nullable PerxUserGroupService userGroupService;
  private @Nullable PerxGroupUpdateTask groupUpdateTask;
  private @Nullable ConfigManager configManager;
  private @Nullable PerxSignHandler signManager;

  private Logger logger = Bukkit.getLogger();

  private Perx() {}

  public static Perx getInstance() {
    if (instance != null)
      return instance;
    synchronized (lock) {
      if (instance != null)
        return instance;
      instance = new Perx();
      return instance;
    }
  }

  public static Plugin getPlugin() {
    @Nullable Plugin plugin = getInstance().plugin;
    Preconditions.checkState(plugin != null, "Perx is not loaded");
    return plugin;
  }

  public static Logger getLogger() {
    return getInstance().logger;
  }

  public boolean isLoaded() {
    return loaded;
  }

  public Database getDatabase() {
    return Preconditions.checkNotNull(database);
  }

  public PerxGroupService getGroupService() {
    return Preconditions.checkNotNull(groupService);
  }

  public PerxGroupHandler getGroupHandler() {
    return Preconditions.checkNotNull(groupHandler);
  }

  public PerxUserGroupService getUserGroupService() {
    return Preconditions.checkNotNull(userGroupService);
  }

  public PerxUserService getUserService() {
    return Preconditions.checkNotNull(userService);
  }

  public PerxGroupUpdateTask getGroupUpdateTask() {
    return Preconditions.checkNotNull(groupUpdateTask);
  }

  public ConfigManager getConfigManager() {
    return Preconditions.checkNotNull(configManager);
  }

  public PerxSignHandler getSignManager() {
    return Preconditions.checkNotNull(signManager);
  }

  public MessageRepository getMessages() {
    return messages;
  }

  public PermissionAdapterFactory getPermissionAdapterFactory() {
    return Objects.requireNonNull(permissionAdapterFactory, "PermissionAdapter factory is null");
  }

  @CanIgnoreReturnValue
  public boolean load(
      Plugin plugin,
      Database database,
      GroupStyleExecutor styleExecutor,
      PermissionAdapterFactory factory) {
    Preconditions.checkNotNull(plugin, "Plugin");
    Preconditions.checkNotNull(database, "Database");
    Preconditions.checkNotNull(styleExecutor, "Style executor");
    Preconditions.checkNotNull(factory, "PermissionAdapter factory");
    synchronized (lock) {
      if (loaded) return false;
      try {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.database = database;
        this.permissionAdapterFactory = factory;
        listeners.forEach((x) -> Bukkit.getPluginManager().registerEvents(x, plugin));
        // service & manager allocation
        (this.configManager = new ConfigManager(plugin.getDataFolder())).load();
        (this.groupService = new PerxGroupManager(database)).load();
        (this.userGroupService = new PerxUserGroupManager(database)).load();
        this.userService = new PerxUserManager(database, userGroupService);
        this.groupHandler = new PerxGroupHandler(
            database, styleExecutor, userService, groupService, userGroupService);
        (this.groupUpdateTask = new PerxGroupUpdateTask(plugin)).start();
        (this.signManager = new PerxSignHandler(new PerxSignFile(
            new File(plugin.getDataFolder(), ".storage/signs.dat")
        ))).load();
        // finish
        this.loaded = true;
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
    synchronized (lock) {
      if (!loaded) return false;
      try {
        if (groupUpdateTask != null)
          groupUpdateTask.stop();
        if (groupHandler != null && userService != null)
          // reset all players within cache due to unknown next load
          Bukkit.getOnlinePlayers().forEach((player) -> {
            @Nullable PerxUser user = userService.getRepository().get(player);
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
