package io.github.aparx.perx.utils.duration;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 15:43
 * @since 1.0
 */
public final class DurationUtils {

  private DurationUtils() {
    throw new AssertionError();
  }

  public static String createTimeLeft(Date current, Date future) {
    return createTimeLeft(Duration.ofMillis(Math.max(future.getTime() - current.getTime(), 0)));
  }

  public static String createTimeLeft(Duration duration) {
    long days = duration.toDays();
    long hours = (duration.toHours() % 24);
    long minutes = (duration.toMinutes() % 60);
    long seconds = (duration.toSeconds() % 60);
    List<String> units = new ArrayList<>();
    if (days > 0) units.add(days + "d");
    if (hours > 0) units.add(hours + "h");
    if (minutes > 0) units.add(minutes + "m");
    if (seconds > 0) units.add(seconds + "s");
    return String.join(StringUtils.SPACE, units);
  }

}
