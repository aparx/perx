package io.github.aparx.perx.events;

import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.event.Cancellable;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-11 18:06
 * @since 1.0
 */
public class CancellablePerxGroupEvent extends PerxGroupEvent implements Cancellable {

  private boolean cancel;

  public CancellablePerxGroupEvent(PerxGroup group) {
    super(group);
  }

  @Override
  public boolean isCancelled() {
    return cancel;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancel = cancel;
  }
}
