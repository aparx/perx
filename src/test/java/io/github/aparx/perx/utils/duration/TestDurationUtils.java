package io.github.aparx.perx.utils.duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 07:58
 * @since 1.0
 */
public class TestDurationUtils {

  @Test
  public void testCreateTimeLeft() throws DurationParseException {
    assertCreateTimeLeft("10d 7m 23s", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("4d 7m 23s", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("3d 7m", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("3d 10m", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("10s", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("10m", DurationUtils.DEFAULT_UNIT_LOOKUP);
    assertCreateTimeLeft("476d", DurationUtils.DEFAULT_UNIT_LOOKUP);
  }

  private void assertCreateTimeLeft(String expected, DurationUtils.UnitLookup lookup)
      throws DurationParseException {
    Assertions.assertEquals(DurationUtils.createTimeLeft(
        DurationParser.DEFAULT_PARSER.parse(expected), lookup
    ), expected);
  }

}
