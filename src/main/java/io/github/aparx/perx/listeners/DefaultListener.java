package io.github.aparx.perx.listeners;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.utils.ArrayPath;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
public final class DefaultListener implements Listener {

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

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    boolean cancelJoinMessage = event.getJoinMessage() == null;
    event.setJoinMessage(null); // broadcast join message later
    Perx.getInstance().getGroupHandler()
        .reinitializePlayer(event.getPlayer())
        .whenComplete((__, ex) -> {
          if (ex == null && !cancelJoinMessage && player.isOnline())
            Bukkit.broadcastMessage(Message.JOIN.substitute(new LookupPopulator()
                .put(ArrayPath.of("player"), event.getPlayer())
                .getLookup()));
        });
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent event) {
    if (event.getQuitMessage() != null)
      event.setQuitMessage(Message.QUIT.get().substitute(new LookupPopulator()
          .put(ArrayPath.of("player"), event.getPlayer())
          .getLookup()));
    // remove the quitting player from cache
    UUID uuid = event.getPlayer().getUniqueId();
    // TODO: clear cache of offline people on interval instead
    Perx.getInstance().getUserController().remove(uuid);
    Perx.getInstance().getUserGroupController().removeByUser(uuid);
  }


}
