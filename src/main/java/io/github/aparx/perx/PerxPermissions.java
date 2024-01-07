package io.github.aparx.perx;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 05:43
 * @since 1.0
 */
public final class PerxPermissions {

  public static final String PERMISSION_SIGN = "perx.sign";
  public static final String PERMISSION_MANAGE = "perx.manage";
  public static final String PERMISSION_INFO_SELF = "perx.info.self";
  public static final String PERMISSION_INFO_OTHER = "perx.info.other";

  private PerxPermissions() {
    throw new AssertionError();
  }

}
