package io.github.aparx.perx.events;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.group.PerxGroupController;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Bukkit event called whenever Perx has fetched and registered all concurrently existing groups.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 09:16
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupsFetchedEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();

  private final PerxGroupController controller;

  public GroupsFetchedEvent(PerxGroupController controller, boolean isAsync) {
    super(isAsync);
    Preconditions.checkNotNull(controller, "Controller must not be null");
    this.controller = controller;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }

  public PerxGroupController getController() {
    return controller;
  }
}
