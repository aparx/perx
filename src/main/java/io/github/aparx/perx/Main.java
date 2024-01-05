package io.github.aparx.perx;

import io.github.aparx.perx.database.PerxDatabase;
import io.github.aparx.perx.group.PerxGroupBuilder;
import io.github.aparx.perx.group.PerxGroupController;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.many.PerxUserGroup;
import io.github.aparx.perx.group.many.PerxUserGroupController;
import io.github.aparx.perx.group.style.ScoreboardGroupStyleExecutor;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:22
 * @since 1.0
 */
public final class Main extends JavaPlugin implements Listener {

  private @Nullable ScoreboardGroupStyleExecutor styleExecutor;

  @Override
  public void onEnable() {
    PerxDatabase database = new PerxDatabase();
    this.styleExecutor = new ScoreboardGroupStyleExecutor();
    if (!Perx.getInstance().load(this, database, styleExecutor))
      throw new IllegalStateException("Could not load Perx");
    database.connect("url", "username", "password").thenRun(() -> Perx.getLogger().info("Finished loading database"));
    Bukkit.getPluginManager().registerEvents(this, this);
    PerxGroupBuilder.builder("admin")
        .priority(100)
        .prefix("§7[§4Admin§7] §r")
        .addPermissions("skywarz.setup")
        .build()
        .push();

    PerxGroupBuilder.builder("mod")
        .priority(10)
        .prefix("§7[§aMod§7] §r")
        .addPermissions("mod.publish.*")
        .addPermissions("mod.*")
        .build()
        .push();
  }

  @Override
  public void onDisable() {
    if (styleExecutor != null && Perx.getInstance().isLoaded())
      styleExecutor.clear();
    if (!Perx.getInstance().unload())
      throw new IllegalStateException("Could not unload Perx");
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    PerxGroupHandler handler = Perx.getInstance().getGroupHandler();

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.MINUTE, 1);
    handler.subscribe(event.getPlayer().getUniqueId(), "admin", calendar.getTime())
        .thenAccept((x) -> event.getPlayer().sendMessage(String.valueOf(x)));
    Bukkit.getScheduler().runTaskLater(Perx.getPlugin(), () -> {
      long time = System.currentTimeMillis();
      Perx.getInstance().getUserController()
          .fetch(event.getPlayer(), UserCacheStrategy.AUTO)
          .thenAccept((user) -> {
            if (user != null && event.getPlayer().isOnline()) {
              event.getPlayer().sendMessage("§e" + user.getSubscribed());
              event.getPlayer().sendMessage("Took: " + (System.currentTimeMillis() - time) + "ms");
            }
          });
    }, 5 * 20L);
  }
}
