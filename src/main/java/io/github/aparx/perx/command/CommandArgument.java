package io.github.aparx.perx.command;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 02:21
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public record CommandArgument(String value) {

  public CommandArgument {
    Preconditions.checkNotNull(value, "Value must not be null");
  }

  @SuppressWarnings("deprecation")
  public OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(value);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    CommandArgument that = (CommandArgument) object;
    return Objects.equals(value, that.value);
  }

}
