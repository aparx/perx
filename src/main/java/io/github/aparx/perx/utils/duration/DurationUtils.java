package io.github.aparx.perx.utils.duration;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 15:43
 * @since 1.0
 */
public final class DurationUtils {

  public static final UnitLookup DEFAULT_UNIT_LOOKUP = UnitLookup.of(Map.of(
      DisplayUnit.SECONDS, "s",
      DisplayUnit.MINUTES, "m",
      DisplayUnit.HOURS, "h",
      DisplayUnit.DAYS, "d"
  ));

  private DurationUtils() {
    throw new AssertionError();
  }

  public static String createTimeLeft(Date current, Date future) {
    return createTimeLeft(Duration.ofMillis(Math.max(future.getTime() - current.getTime(), 0)));
  }

  public static String createTimeLeft(Duration duration) {
    return createTimeLeft(duration, DEFAULT_UNIT_LOOKUP);
  }

  public static String createTimeLeft(Duration duration, UnitLookup lookup) {
    long days = duration.toDays();
    long hours = (duration.toHours() % 24);
    long minutes = (duration.toMinutes() % 60);
    long seconds = (duration.toSeconds() % 60);
    List<String> units = new ArrayList<>();
    if (days > 0)
      units.add(days + lookup.lookup(DisplayUnit.DAYS));
    if (hours > 0)
      units.add(hours + lookup.lookup(DisplayUnit.HOURS));
    if (minutes > 0)
      units.add(minutes + lookup.lookup(DisplayUnit.MINUTES));
    if (seconds > 0)
      units.add(seconds + lookup.lookup(DisplayUnit.SECONDS));
    return String.join(StringUtils.SPACE, units);
  }

  public enum DisplayUnit {
    DAYS,
    HOURS,
    MINUTES,
    SECONDS
  }

  @FunctionalInterface
  public interface UnitLookup {
    @Nullable String lookup(DisplayUnit unit);

    static UnitLookup of(Map<DisplayUnit, ?> map) {
      return (unit) -> Objects.toString(map.get(unit), null);
    }
  }

}
