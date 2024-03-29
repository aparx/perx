package io.github.aparx.perx.sign;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.group.intersection.PerxUserGroup;
import io.github.aparx.perx.user.PerxUser;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 17:21
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxSignHandler implements Listener {

  private final SignUpdater updater = new SignUpdater();

  private final PerxSignStorage storage;

  public PerxSignHandler(PerxSignStorage storage) {
    Preconditions.checkNotNull(storage, "Storage must not be null");
    this.storage = storage;
  }

  public static boolean isSignBlock(Location location) {
    return location.getBlock().getState() instanceof Sign;
  }

  public PerxSignStorage getStorage() {
    return storage;
  }

  public void load() {
    storage.read().thenCompose((__) -> storage.save()).thenAccept((__) -> {
      if (Perx.getPlugin().isEnabled())
        Bukkit.getPluginManager().registerEvents(this, Perx.getPlugin());
      Bukkit.getScheduler().runTaskTimer(Perx.getPlugin(), updater, 0, 30);
    });
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onSignChange(SignChangeEvent event) {
    Player player = event.getPlayer();
    if (player.hasPermission(PerxPermissions.PERMISSION_SIGN)
        && "[perx]".equalsIgnoreCase(event.getLine(0))) {
      event.setCancelled(true);
      storage.add(PerxSign.of(event.getBlock().getLocation()));
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    BlockState state = event.getBlock().getState();
    if (!(state instanceof Sign)) return;
    PerxSignRepository repository = storage.getRepository();
    if (!repository.contains(state.getLocation())) return;
    Player player = event.getPlayer();
    if (player.hasPermission(PerxPermissions.PERMISSION_SIGN))
      storage.remove(state.getLocation());
    else event.setCancelled(true);
  }

  final class SignUpdater implements Runnable {

    static int MAX_SIGN_CHAR_LENGTH = 30;

    long ticks;

    @Override
    public void run() {
      PerxSignRepository repository = storage.getRepository();
      if (repository.isEmpty()) return;
      for (Player player : Bukkit.getOnlinePlayers())
        repository.getInWorld(player.getWorld())
            .forEach((sign) -> updateSign(player, sign, ticks));
      ++ticks;
    }

    void updateSign(Player player, PerxSign sign, long ticks) {
      @Nullable Location location = sign.getLocation();
      Preconditions.checkNotNull(location, "Location has become null");
      if (!isSignBlock(location)) {
        storage.remove(location);
        return;
      }
      String[] lines = {
          "§e[Perx]",
          "Your groups:",
          "-",
          StringUtils.SPACE
      };
      @Nullable PerxUser user = Perx.getInstance().getUserService().getRepository().get(player);
      if (user == null) {
        player.sendSignChange(location, lines);
        return;
      }
      Collection<PerxUserGroup> subscribed = user.getSubscribed();
      Iterator<PerxUserGroup> iterator = subscribed.iterator();
      if (!subscribed.isEmpty()) {
        int target = (int) (ticks % subscribed.size());
        @Nullable PerxUserGroup userGroup = null;
        for (int i = 0; iterator.hasNext() && i <= target; ++i)
          userGroup = iterator.next();
        if (userGroup != null)
          lines[2] = ChatColor.BOLD + StringUtils.capitalize(userGroup.getGroupName());
        lines[3] = createScrollbar(subscribed.size(), target);
      }
      player.sendSignChange(location, lines);
    }

    String createScrollbar(int size, int index) {
      StringBuilder builder = new StringBuilder();
      int show = Math.min(MAX_SIGN_CHAR_LENGTH, size);
      index = Math.max(Math.min(index, show - 1), 0);
      builder.append("•".repeat(Math.max(0, show)));
      builder.insert(index, ChatColor.GRAY);
      builder.insert(3 + index, ChatColor.RESET);
      builder.insert(0, ChatColor.RESET);
      return builder.toString();
    }

  }


}