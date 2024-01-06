package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.user.controller.PerxUserController;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 02:37
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxGroupUpdateTask {

  private final Plugin plugin;

  private @Nullable BukkitTask task;

  public PerxGroupUpdateTask(Plugin plugin) {
    Preconditions.checkNotNull(plugin, "Plugin must not be null");
    this.plugin = plugin;
  }

  public void start() {
    if (task != null) task.cancel();
    task = Bukkit.getScheduler().runTaskTimer(plugin, this::perform, 20, 30);
  }

  public void stop() {
    if (task != null) task.cancel();
    task = null;
  }

  /**
   * Performs a group update for all players and automatically unsubscribes if needed.
   */
  public void perform() {
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    PerxUserController userController = Perx.getInstance().getUserController();
    Bukkit.getOnlinePlayers().stream()
        .map(userController::get)
        .filter(Objects::nonNull)
        .flatMap((user) -> user.getSubscribed().stream())
        .forEach(groupHandler::unsubscribeIfPastEndDate);
  }

}
