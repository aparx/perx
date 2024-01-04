package io.github.aparx.perx.group.style;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Interface that applies a style of a group to a target permissible.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 02:36
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public interface GroupStyleExecutor {

  /**
   * Applies style of {@code group} onto {@code permissible} and returns true if the style could
   * be applied, false otherwise.
   *
   * @param group       the group to style {@code permissible} with
   * @param permissible the target that should be styled
   * @return true if the style could be applied to {@code permissible}
   */
  @CanIgnoreReturnValue
  boolean apply(PerxGroup group, Permissible permissible);

  /** Removes the style from a group and permissible */
  @CanIgnoreReturnValue
  boolean remove(PerxGroup group, Permissible permissible);

}
