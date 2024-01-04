package io.github.aparx.perx.listeners;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.user.UserCacheStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 11:42
 * @since 1.0
 */
public final class UpdateListener implements Listener {

  @EventHandler
  public void onLoad(GroupsFetchedEvent event) {
    Bukkit.getOnlinePlayers().forEach(this::updatePlayer);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onConnect(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED)
      // fetch the user on login, so the user is fresh near join
      Perx.getInstance().getUserController()
          .fetch(event.getUniqueId(), UserCacheStrategy.AUTO);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent event) {
    updatePlayer(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent event) {
    // remove the quitting player from cache
    // TODO: clear cache of offline people on interval instead
    Perx.getInstance().getUserController().remove(event.getPlayer());
  }

  private void updatePlayer(Player player) {
    Perx.getInstance().getUserController()
        .fetch(player.getUniqueId(), UserCacheStrategy.AUTO)
        .thenAccept((user) -> {
          if (user != null)
            user.getSubscribed().stream().sorted().forEach((x) -> {
              Perx.getInstance().getGroupHandler().applyGroupSync(player, x);
            });
        });
  }


}
