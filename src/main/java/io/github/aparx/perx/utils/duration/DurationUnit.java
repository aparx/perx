package io.github.aparx.perx.utils.duration;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntToLongFunction;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:59
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class DurationUnit implements Iterable<String> {

  public static DurationUnit SECONDS = DurationUnit.of(
      new String[]{"s", "sec", "secs", "second", "seconds"},
      (amount) -> amount * 1000L);

  public static DurationUnit MINUTES = DurationUnit.of(
      new String[]{"m", "min", "mins", "minute", "minutes"},
      (amount) -> 60 * SECONDS.mapToMillis(amount));

  public static DurationUnit HOURS = DurationUnit.of(
      new String[]{"h", "hour", "hours"},
      (amount) -> 60 * MINUTES.mapToMillis(amount));

  public static DurationUnit DAYS = DurationUnit.of(
      new String[]{"d", "day", "days"},
      (amount) -> 24 * HOURS.mapToMillis(amount));

  private final String[] literals;
  private final IntToLongFunction milliMapper;

  private DurationUnit(
      String[] literals,
      IntToLongFunction milliMapper) {
    Preconditions.checkNotNull(milliMapper, "Millisecond mapper must not be null");
    this.literals = literals.clone();
    this.milliMapper = milliMapper;
  }

  public static DurationUnit of(
      String[] literals,
      IntToLongFunction milliMapper) {
    Validate.notEmpty(literals, "Literals must not be empty");
    Validate.noNullElements(literals, "Literal(s) must not be null");
    return new DurationUnit(literals, milliMapper);
  }

  public long mapToMillis(int amount) {
    return milliMapper.applyAsLong(amount);
  }

  public String[] getLiterals() {
    return literals.clone();
  }

  @Override
  public Iterator<String> iterator() {
    return List.of(literals).iterator();
  }
}
