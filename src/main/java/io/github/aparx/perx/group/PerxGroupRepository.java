package io.github.aparx.perx.group;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 06:59
 * @since 1.0
 */
public interface PerxGroupRepository extends Iterable<PerxGroup> {

  int size();

  /** Puts {@code group} if not already registered */
  @CanIgnoreReturnValue
  boolean register(PerxGroup group);

  /** Puts {@code group} and overrides any previous mapping */
  @CanIgnoreReturnValue
  @Nullable PerxGroup put(PerxGroup group);

  @CanIgnoreReturnValue
  boolean remove(PerxGroup group);

  @CanIgnoreReturnValue
  boolean remove(String name);

  boolean contains(PerxGroup group);

  boolean contains(String name);

  @Nullable PerxGroup get(String name);

  default Stream<PerxGroup> stream() {
    return StreamSupport.stream(Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL), false);
  }

  default PerxGroup getLoudly(String name) {
    @Nullable PerxGroup group = get(name);
    Preconditions.checkArgument(group != null, "Group " + name + " unknown");
    return group;
  }

}
