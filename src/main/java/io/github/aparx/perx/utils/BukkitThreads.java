package io.github.aparx.perx.utils;

import io.github.aparx.perx.Perx;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 04:58
 * @since 1.0
 */
public final class BukkitThreads {

  private BukkitThreads() {
    throw new AssertionError();
  }

  public static void runOnPrimaryThread(Runnable runnable) {
    runOnPrimaryThread(Perx.getPlugin(), runnable);
  }

  public static void runOnPrimaryThread(Plugin plugin, Runnable runnable) {
    if (!Bukkit.isPrimaryThread())
      Bukkit.getScheduler().runTask(plugin, runnable);
    else runnable.run();
  }

}
