package io.github.aparx.perx.group.style;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:33
 * @since 1.0
 */
public enum GroupStyleKey {
  /** The applied prefix for a player in a group */
  PREFIX(32),
  /** The applied suffix for a player in a group */
  SUFFIX(PREFIX.getMaxLength());

  private final int maxLength;

  GroupStyleKey(int maxLength) {
    this.maxLength = maxLength;
  }

  public int getMaxLength() {
    return maxLength;
  }
}
