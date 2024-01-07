package io.github.aparx.perx.utils.duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 07:20
 * @since 1.0
 */
public class TestDurationParser {

  @Test
  public void testParse() throws DurationParseException {
    DurationParser parser = DurationParser.DEFAULT_PARSER;
    assertDuration(parser.parse("7d3m2s"), 7, 0, 3, 2);
    Assertions.assertThrows(DurationParseException.class, () -> parser.parse("37 d 3 m 2.5 s"));
    Assertions.assertThrows(DurationParseException.class, () -> parser.parse("-5 s"));
    Assertions.assertThrows(DurationParseException.class, () -> parser.parse("_ 5 s"));
    Assertions.assertThrows(DurationParseException.class, () -> parser.parse(";5 s"));
    assertDuration(parser.parse("37 d 3 m 25 s"), 37, 0, 3, 25);
    assertDuration(parser.parse("1h2m"), 1, 2, 0);
    assertDuration(parser.parse("1hour2m"), 1, 2, 0);
    assertDuration(parser.parse("1hours 2m"), 1, 2, 0);
    assertDuration(parser.parse("1hours 2min"), 1, 2, 0);
    assertDuration(parser.parse("1hours 2 min"), 1, 2, 0);
    assertDuration(parser.parse("1d2d2m"), 2, 0, 2, 0);
    assertDuration(parser.parse("1dAys2"), 1, 0, 0, 0);
    assertDuration(parser.parse("1hAys2s"), 1, 0, 2);
    assertDuration(parser.parse("1y2d"), 12, 0, 0, 0);
    assertDuration(parser.parse("1minutes 2hours"), 2, 1, 0);
    assertDuration(parser.parse("4d7m23s"), 4, 0, 7, 23);
    assertDuration(parser.parse("4d 7 Min 23s"), 4, 0, 7, 23);
    assertDuration(parser.parse("4d 7 Min 23 Secs"), 4, 0, 7, 23);
    assertDuration(parser.parse("4 Days 7 Min 23 Secs"), 4, 0, 7, 23);
    assertDuration(parser.parse("4 days 7 mins 23 secs"), 4, 0, 7, 23);
    assertDuration(parser.parse("4 d 7 m 23 s"), 4, 0, 7, 23);
  }

  public void assertDuration(Duration duration, int hours, int minutes, int seconds) {
    assertDuration(duration, hours / 24, hours, minutes, seconds);
  }

  public void assertDuration(Duration duration, int days, int hours, int minutes, int seconds) {
    Assertions.assertNotNull(duration);
    Assertions.assertEquals(days, duration.toDays());
    Assertions.assertEquals(hours, duration.toHours() % 24);
    Assertions.assertEquals(minutes, duration.toMinutes() % 60);
    Assertions.assertEquals(seconds, duration.toSeconds() % 60);
  }

}
