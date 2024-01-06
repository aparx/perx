package io.github.aparx.perx.sign;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.io.*;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 16:54
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class PerxSign implements Serializable, Cloneable {

  @Serial
  private static final long serialVersionUID = 9223372036854775807L;

  private @Nullable Location location;

  public PerxSign() {}

  public PerxSign(Location location) {
    Preconditions.checkNotNull(location, "Location must not be null");
    this.location = location;
  }

  public @Nullable Location getLocation() {
    return location;
  }

  @Override
  public PerxSign clone() {
    try {
      PerxSign sign = (PerxSign) super.clone();
      sign.location = (location != null ? location.clone() : null);
      return sign;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    PerxSign sign = (PerxSign) object;
    return Objects.equals(location, sign.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(location);
  }

  @Override
  public String toString() {
    return "PerxSign{" +
        "location=" + location +
        '}';
  }

  @Serial
  private void writeObject(ObjectOutputStream stream) throws IOException {
    Preconditions.checkNotNull(location, "Location is not given");
    @Nullable World world = location.getWorld();
    Preconditions.checkNotNull(world, "World must not be null");
    stream.writeDouble(location.getX());
    stream.writeDouble(location.getY());
    stream.writeDouble(location.getZ());
    stream.writeUTF(world.getName());
  }

  @Serial
  private void readObject(ObjectInputStream stream) throws IOException {
    if (location == null) location = new Location(null, 0, 0, 0);
    location.setX(stream.readDouble());
    location.setY(stream.readDouble());
    location.setZ(stream.readDouble());
    location.setWorld(Bukkit.getWorld(stream.readUTF()));
  }
}
