package io.github.aparx.perx.command.args;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

  public @Nullable Player findPlayer() {
    return Bukkit.getPlayer(value);
  }

  public Player getPlayer() {
    @Nullable Player player = findPlayer();
    Preconditions.checkNotNull(player, "Invalid player");
    return player;
  }

  public boolean getBoolean() {
    return Boolean.parseBoolean(value);
  }

  public int getInt() {
    return NumberConversions.toInt(value);
  }

  public long getLong() {
    return NumberConversions.toLong(value);
  }

  public double getDouble() {
    return NumberConversions.toDouble(value);
  }

  public @Nullable PerxGroup getGroup() {
    return Perx.getInstance().getGroupService().getRepository().get(value);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    CommandArgument that = (CommandArgument) object;
    return Objects.equals(value, that.value);
  }

}
