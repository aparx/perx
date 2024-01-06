package io.github.aparx.perx.sign;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.PerxUser;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
public final class PerxSignManager implements Listener {

  private final PerxSignHandler handler;
  private final SignUpdater updater = new SignUpdater();

  public PerxSignManager(File dataFolder) {
    this(new PerxSignHandler(new File(dataFolder, "signs.dat")));
  }

  public PerxSignManager(PerxSignHandler handler) {
    Preconditions.checkNotNull(handler, "Handler must not be null");
    this.handler = handler;
  }

  public PerxSignHandler getHandler() {
    return handler;
  }

  public void load() {
    handler.readAsync().thenCompose((__) -> handler.saveAsync()).thenAccept((__) -> {
      if (Perx.getPlugin().isEnabled())
        Bukkit.getPluginManager().registerEvents(this, Perx.getPlugin());
      Bukkit.getScheduler().runTaskTimer(Perx.getPlugin(), updater, 0, 20);
    });
  }


  @EventHandler(priority = EventPriority.HIGH)
  void onSignChange(SignChangeEvent event) {
    Player player = event.getPlayer();
    if (player.hasPermission(PerxCommand.PERMISSION_MANAGE)
        && "[perx]".equalsIgnoreCase(event.getLine(0))) {
      event.setCancelled(true);
      handler.add(event.getBlock().getLocation());
      handler.saveAsync();
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  void onBreak(BlockBreakEvent event) {
    if (event.isCancelled()) return;
    BlockState state = event.getBlock().getState();
    if (!(state instanceof Sign)) return;
    if (!handler.isSign(state.getLocation())) return;
    Player player = event.getPlayer();
    if (player.hasPermission(PerxCommand.PERMISSION_MANAGE)) {
      handler.remove(state.getLocation());
      handler.saveAsync();
    } else event.setCancelled(true);
  }

  final class SignUpdater implements Runnable {

    static int MAX_SIGN_CHAR_LENGTH = 30;

    long ticks;

    @Override
    public void run() {
      Bukkit.getOnlinePlayers().forEach((player) -> {
        handler.getInWorld(player.getWorld()).forEach((sign) -> updateSign(player, sign, ticks));
      });
      ++ticks;
    }

    void updateSign(Player player, PerxSign sign, long ticks) {
      @Nullable Location location = sign.getLocation();
      Preconditions.checkNotNull(location, "Location has become null");
      if (!handler.isValid(location)) {
        handler.remove(location);
        handler.saveAsync();
        return;
      }
      String[] lines = new String[4];
      lines[0] = "§e[Perx]";
      @Nullable PerxUser user = Perx.getInstance().getUserController().get(player);
      if (user != null) {
        Collection<PerxUserGroup> subscribed = user.getSubscribed();
        Iterator<PerxUserGroup> iterator = subscribed.iterator();
        int target = !subscribed.isEmpty() ? (int) (ticks % subscribed.size()) : 0;
        @Nullable PerxUserGroup userGroup = null;
        for (int i = 0; iterator.hasNext() && i <= target; ++i)
          userGroup = iterator.next();
        lines[1] = "Your groups:";
        if (userGroup != null)
          lines[2] = ChatColor.BOLD + StringUtils.capitalize(userGroup.getGroupName());
        lines[3] = createScrollbar(subscribed.size(), target);
      }
      player.sendSignChange(location, lines);
    }

    String createScrollbar(int size, int index) {
      StringBuilder builder = new StringBuilder();
      int show = Math.min(MAX_SIGN_CHAR_LENGTH, size);
      index = Math.min(index, show - 1);
      builder.append("•".repeat(Math.max(0, show)));
      builder.insert(index, ChatColor.GRAY);
      builder.insert(3 + index, ChatColor.RESET);
      builder.insert(0, ChatColor.RESET);
      return builder.toString();
    }

  }


}