package io.github.aparx.perx;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.config.ConfigManager;
import io.github.aparx.perx.config.configs.DatabaseConfig;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.PerxDatabase;
import io.github.aparx.perx.group.PerxGroupBuilder;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.style.ScoreboardGroupStyleExecutor;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.utils.BukkitThreads;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:22
 * @since 1.0
 */
public final class Main extends JavaPlugin implements Listener {

  private @Nullable ScoreboardGroupStyleExecutor styleExecutor;

  @Override
  public void onEnable() {
    Bukkit.getPluginManager().registerEvents(this, this);

    Perx perx = Perx.getInstance();
    PerxDatabase database = new PerxDatabase();
    // Call `clear` on style executor in case disable was not called before
    (this.styleExecutor = new ScoreboardGroupStyleExecutor()).clear();
    if (!perx.load(this, database, styleExecutor))
      throw new IllegalStateException("Could not load Perx");
    connectDatabase(database).exceptionally((ex) -> {
      Perx.getLogger().log(Level.SEVERE, "Error with database (forgot setup?)", ex);
      BukkitThreads.runOnPrimaryThread(() -> Bukkit.getPluginManager().disablePlugin(this));
      return null;
    });
    // next register the command
    PerxCommand perxCommand = PerxCommand.getInstance();
    String commandName = perxCommand.getRoot().getInfo().name();
    PluginCommand command = getCommand(commandName);
    Preconditions.checkNotNull(command, "Command " + commandName + " unknown");
    command.setTabCompleter(PerxCommand.getInstance());
    command.setExecutor(PerxCommand.getInstance());
  }

  @Override
  public void onDisable() {
    if (styleExecutor != null && Perx.getInstance().isLoaded())
      styleExecutor.clear();
    if (!Perx.getInstance().unload())
      throw new IllegalStateException("Could not unload Perx");
  }

  private CompletableFuture<?> connectDatabase(Database database) {
    ConfigManager configManager = Perx.getInstance().getConfigManager();
    DatabaseConfig config = configManager.getDatabaseConfig();
    return database.connect(config.getURL(), config.getUsername(), config.getPassword())
        .thenRun(() -> Perx.getLogger().info("Finished loading database"));
  }

  @Deprecated
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    PerxGroupHandler handler = Perx.getInstance().getGroupHandler();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DAY_OF_MONTH, 3);
    handler.subscribe(event.getPlayer().getUniqueId(), "admin", calendar.getTime())
        .thenAccept((x) -> event.getPlayer().sendMessage(String.valueOf(x)));
    Bukkit.getScheduler().runTaskLater(Perx.getPlugin(), () -> {
      Perx.getInstance().getUserController()
          .fetchOrGet(event.getPlayer(), UserCacheStrategy.AUTO)
          .thenAccept((user) -> {
            Player player = user.getPlayer();
            if (player != null)
              player.sendMessage(user.getSubscribed().stream()
                  .map(PerxUserGroup::getGroupName)
                  .toList()
                  .toString());
          });
    }, 1 * 20L);
  }
}
