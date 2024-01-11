package io.github.aparx.perx.events;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.group.PerxGroup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Cancellable event called by the {@code GroupModelDao} before a {@code PerxGroup} is created,
 * upsert, updated or deleted to the database.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-11 12:04
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxGroupMutateEvent extends CancellablePerxGroupEvent {

  private final PerxMutateType type;

  public PerxGroupMutateEvent(PerxMutateType type, PerxGroup group) {
    super(group);
    Preconditions.checkNotNull(type, "Type must not be null");
    this.type = type;
  }

  public PerxMutateType getType() {
    return type;
  }

  public boolean isType(PerxMutateType type) {
    return this.type == type;
  }

}
