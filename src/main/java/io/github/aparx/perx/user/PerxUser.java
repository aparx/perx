package io.github.aparx.perx.user;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.utils.WeakHashSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 03:38
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxUser implements Iterable<PerxGroup> {

  private final UUID id;

  /** Set of group names this user is subscribed to */
  private final WeakHashSet<PerxGroup> subscribed;

  public PerxUser(UUID id) {
    this(id, new WeakHashSet<>());
  }

  public PerxUser(UUID id, WeakHashSet<PerxGroup> subscribed) {
    Preconditions.checkNotNull(id, "ID must not be null");
    Preconditions.checkNotNull(subscribed, "Set must not be null");
    this.id = id;
    this.subscribed = subscribed;
  }

  public UUID getId() {
    return id;
  }

  public OfflinePlayer getOffline() {
    return Bukkit.getOfflinePlayer(id);
  }

  public @Nullable Player getPlayer() {
    return Bukkit.getPlayer(id);
  }

  public WeakHashSet<PerxGroup> getSubscribed() {
    return subscribed;
  }

  public void update() {
    @Nullable Player player = getPlayer();
    if (player == null) return;
    getSubscribed().stream().sorted().forEach((group) -> {
      Perx.getInstance().getGroupHandler().applyGroupSync(player, group);
    });
  }

  @Override
  public Iterator<PerxGroup> iterator() {
    return subscribed.iterator();
  }

  @Override
  public String toString() {
    return "PerxUser{" +
        "id=" + id +
        ", subscribed=" + subscribed +
        '}';
  }
}
