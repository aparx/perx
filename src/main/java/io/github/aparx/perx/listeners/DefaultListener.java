package io.github.aparx.perx.listeners;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.events.GroupsFetchedEvent;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.utils.ArrayPath;
import io.github.aparx.perx.utils.BukkitThreads;
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
    // we just may want to ensure we run on the primary thread, due to the event's call source
    BukkitThreads.runOnPrimaryThread(() -> Bukkit.getOnlinePlayers().forEach((player) -> {
      Perx.getInstance().getGroupHandler().reinitializePlayer(player);
    }));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onConnect(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED)
      // fetch the user on login, so the user is fresh near join
      Perx.getInstance().getUserService()
          .getOrFetch(event.getUniqueId(), UserCacheStrategy.AUTO);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (event.getJoinMessage() == null) return;
    event.setJoinMessage(null); // broadcast join message later
    Perx.getInstance().getGroupHandler()
        .reinitializePlayer(event.getPlayer())
        .thenAccept((__) -> Bukkit.getScheduler().runTask(Perx.getPlugin(), () -> {
          // we delayed this one more tick, due to synchronization with the cache
          if (player.isOnline())
            Bukkit.broadcastMessage(Message.JOIN.substitute(new LookupPopulator()
                .put(ArrayPath.of("player"), event.getPlayer())
                .getLookup()));
        }));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent event) {
    if (event.getQuitMessage() != null)
      event.setQuitMessage(Message.QUIT.get().substitute(new LookupPopulator()
          .put(ArrayPath.of("player"), event.getPlayer())
          .getLookup()));
    // remove the quitting player from cache
    UUID uuid = event.getPlayer().getUniqueId();
    Perx.getInstance().getUserService().getRepository().remove(uuid);
    Perx.getInstance().getUserGroupService().getRepository().removeByUser(uuid);
  }


}
