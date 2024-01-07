package io.github.aparx.perx.utils.duration;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:58
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class DurationParser implements DurationProcessor {

  public static DurationParser DEFAULT_PARSER = new DurationParser(new DurationUnit[]{
      DurationUnit.DAYS, DurationUnit.HOURS, DurationUnit.MINUTES, DurationUnit.SECONDS
  });

  private final DurationUnit[] units;
  private final ImmutableMap<String, DurationUnit> byLiteral;

  public DurationParser(DurationUnit[] units) {
    Validate.noNullElements(units, "Unit must not be null");
    this.units = units.clone();
    ImmutableMap.Builder<String, DurationUnit> mapBuilder = ImmutableMap.builder();
    for (DurationUnit unit : units)
      unit.forEach((literal) -> {
        char[] array = literal.toCharArray();
        for (int i = 0; i < array.length; ++i) {
          if (!Character.isLetter(array[i]))
            throw new IllegalArgumentException("Literal " + literal + " must only contain letters");
          array[i] = Character.toLowerCase(array[i]);
        }
        mapBuilder.put(new String(array), unit);
      });
    this.byLiteral = mapBuilder.build();
  }

  public @Nullable DurationUnit getByLiteral(String literal) {
    return byLiteral.get(literal);
  }

  @Override
  public Duration parse(String string) throws DurationParseException {
    // whoever reads this, I have to flex that this actually worked flawlessly first try
    StringBuilder buffer = new StringBuilder();
    int length = string.length();
    int previousNumber = 0;
    Map<DurationUnit, Integer> unitMap = new HashMap<>();
    for (int i = 0; i < length; ++i) {
      char ch = string.charAt(i);
      if (ch >= '0' && ch <= '9') {
        if (!buffer.isEmpty()) buffer = new StringBuilder();
        previousNumber = 10 * previousNumber + (ch - '0');
      } else if (Character.isLetter(ch)) {
        buffer.append(Character.toLowerCase(ch));
        @Nullable DurationUnit unit = getByLiteral(buffer.toString());
        if (unit != null) {
          buffer = new StringBuilder();
          if (previousNumber != 0)
            unitMap.put(unit, previousNumber);
          previousNumber = 0;
        }
      } else if (!Character.isWhitespace(ch))
        throw new DurationParseException("Malformed format ('" + ch + "' <- disallowed)");
    }
    long totalMillis = 0;
    for (Map.Entry<DurationUnit, Integer> entry : unitMap.entrySet())
      totalMillis += entry.getKey().mapToMillis(entry.getValue());
    return Duration.ofMillis(totalMillis);
  }

  public DurationUnit[] getUnits() {
    return units.clone();
  }

}
