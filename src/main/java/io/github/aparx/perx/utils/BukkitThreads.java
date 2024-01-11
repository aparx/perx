package io.github.aparx.perx.utils;

import io.github.aparx.perx.Perx;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class containing utilities around invoking and handling schedulers using threads.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 04:58
 * @since 1.0
 */
public final class BukkitThreads {

  private BukkitThreads() {
    throw new AssertionError();
  }

  /**
   * Calls {@code runnable} on the primary thread.
   * <p>If this method is called on the primary thread already, then {@code runnable} is
   * immediately invoked. Otherwise, it is delayed until the next synced server tick (being on
   * the primary thread).
   *
   * @param runnable the action executed on the primary thread
   * @see #runOnPrimaryThread(Plugin, Runnable)
   */
  public static void runOnPrimaryThread(Runnable runnable) {
    runOnPrimaryThread(Perx.getPlugin(), runnable);
  }

  /**
   * Calls {@code runnable} on the primary thread.
   * <p>If this method is called on the primary thread already, then {@code runnable} is
   * immediately invoked. Otherwise, it is delayed until the next synced server tick (being on
   * the primary thread).
   *
   * @param plugin   the plugin to handle the potential scheduling
   * @param runnable the action executed on the primary thread
   */
  public static void runOnPrimaryThread(Plugin plugin, Runnable runnable) {
    if (!Bukkit.isPrimaryThread())
      Bukkit.getScheduler().runTask(plugin, runnable);
    else runnable.run();
  }

}
