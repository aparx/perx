package io.github.aparx.perx.events;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.group.PerxGroupService;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Bukkit event called whenever Perx has finished the initial group fetch.
 * <p>The initial fetch happens to sync the database with the memory cache on plugin load.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 09:16
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupsFetchedEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();

  private final PerxGroupService service;

  public GroupsFetchedEvent(PerxGroupService service, boolean isAsync) {
    super(isAsync);
    Preconditions.checkNotNull(service, "Service must not be null");
    this.service = service;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }

  public PerxGroupService getService() {
    return service;
  }
}
