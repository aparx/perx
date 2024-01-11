package io.github.aparx.perx.events;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-11 12:02
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxGroupEvent extends Event {

  private static final HandlerList handlerList = new HandlerList();

  private final PerxGroup group;

  public PerxGroupEvent(PerxGroup group) {
    Preconditions.checkNotNull(group, "Group must not be null");
    this.group = group;
  }

  public static HandlerList getHandlerList() {
    return handlerList;
  }

  @Override
  public HandlerList getHandlers() {
    return handlerList;
  }

  public PerxGroup getGroup() {
    return group;
  }
}
