package io.github.aparx.perx;

import io.github.aparx.perx.config.ConfigManager;
import io.github.aparx.perx.config.configs.DatabaseConfig;
import io.github.aparx.perx.database.Database;
import io.github.aparx.perx.database.PerxDatabase;
import io.github.aparx.perx.group.PerxGroupBuilder;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.style.ScoreboardGroupStyleExecutor;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Calendar;
import java.util.Date;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:22
 * @since 1.0
 */
public final class Main extends JavaPlugin implements Listener {

  private @Nullable ScoreboardGroupStyleExecutor styleExecutor;

  @Override
  public void onEnable() {
    //Bukkit.getPluginManager().registerEvents(this, this);

    Perx perx = Perx.getInstance();
    PerxDatabase database = new PerxDatabase();
    // Call `clear` on style executor in case disable was not called before
    (this.styleExecutor = new ScoreboardGroupStyleExecutor()).clear();
    if (!perx.load(this, database, styleExecutor))
      throw new IllegalStateException("Could not load Perx");
    connectDatabase(database);
    /*PerxGroupBuilder.builder("admin")
        .priority(100)
        .prefix("§7[§4Admin§7] §r")
        .addPermissions("*")
        .build()
        .push();

    PerxGroupBuilder.builder("mod")
        .priority(10)
        .prefix("§7[§aMod§7] §r")
        .addPermissions("mod.publish.*")
        .addPermissions("skywarz.setup")
        .build()
        .push();

    PerxGroupBuilder.builder("user")
        .priority(0)
        .isDefault(true)
        .prefix("§8[User] §r")
        .build()
        .push();*/
  }

  @Override
  public void onDisable() {
    if (styleExecutor != null && Perx.getInstance().isLoaded())
      styleExecutor.clear();
    if (!Perx.getInstance().unload())
      throw new IllegalStateException("Could not unload Perx");
  }

  private void connectDatabase(Database database) {
    ConfigManager configManager = Perx.getInstance().getConfigManager();
    DatabaseConfig config = configManager.getDatabaseConfig();
    database.connect(config.getURL(), config.getUsername(), config.getPassword())
        .thenRun(() -> Perx.getLogger().info("Finished loading database"));
  }

  @Deprecated
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    PerxGroupHandler handler = Perx.getInstance().getGroupHandler();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.MINUTE, 1);
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
