package io.github.aparx.perx.listeners;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 11:42
 * @since 1.0
 */
public final class UpdateListener implements Listener {

  @EventHandler
  public void onLoad(GroupsFetchedEvent ignored) {
    Bukkit.getOnlinePlayers().forEach((player) -> {
      Perx.getInstance().getGroupHandler().reinitializePlayer(player);
    });
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onConnect(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED)
      // fetch the user on login, so the user is fresh near join
      Perx.getInstance().getUserController()
          .fetchOrGet(event.getUniqueId(), UserCacheStrategy.AUTO);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent event) {
    Perx.getInstance().getGroupHandler().reinitializePlayer(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent event) {
    // remove the quitting player from cache
    UUID uuid = event.getPlayer().getUniqueId();
    // TODO: clear cache of offline people on interval instead
    Perx.getInstance().getUserController().remove(uuid);
    Perx.getInstance().getUserGroupController().removeByUser(uuid);
  }


}
