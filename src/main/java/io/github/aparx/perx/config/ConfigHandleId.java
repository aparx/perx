package io.github.aparx.perx.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:42
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigHandleId {
  /** Target config identifier */
  String value();
}
