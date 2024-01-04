package io.github.aparx.perx;

import io.github.aparx.perx.database.PerxDatabase;
import io.github.aparx.perx.database.data.many.UserGroupController;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupBuilder;
import io.github.aparx.perx.group.PerxGroupController;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.group.style.ScoreboardGroupStyleExecutor;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

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
    database.connect("url", "username", "password")
        .thenRun(() -> Perx.getLogger().info("Finished loading database"));
    Bukkit.getPluginManager().registerEvents(this, this);
    /*PerxGroupBuilder.builder("admin")
        .priority(100)
        .prefix("§7[§4Admin§7] §r")
        .addPermissions("admin.publish.*")
        .addPermissions("admin.*")
        .build()
        .push();

    PerxGroupBuilder.builder("mod")
        .priority(10)
        .prefix("§7[§aMod§7] §r")
        .addPermissions("mod.publish.*")
        .addPermissions("mod.*")
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

  public void updatePlayer(Player player) {
    Perx perx = Perx.getInstance();
    perx.getDatabase().queue((__) -> perx.getUserController()
        .fetch(player.getUniqueId(), UserCacheStrategy.RUNTIME)
        .thenAccept((user) -> {
          if (user != null)
            user.getSubscribed().forEach((x) -> perx.getGroupHandler().applyGroup(player, x));
        }));
  }

  @EventHandler
  public void onLoad(GroupsFetchedEvent event) {
    Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    /*Perx perx = Perx.getInstance();
    updatePlayer(event.getPlayer());
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    PerxGroupHandler handler = Perx.getInstance().getGroupHandler();
    handler.subscribe(event.getPlayer().getUniqueId(),
        Objects.requireNonNull(groupController.get("mod")));*/
  }
}
