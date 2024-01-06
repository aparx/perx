package io.github.aparx.perx.utils.duration;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 15:24
 * @since 1.0
 */
@FunctionalInterface
public interface DurationProcessor {

  @NonNull Duration parse(@NonNull String string);

}
