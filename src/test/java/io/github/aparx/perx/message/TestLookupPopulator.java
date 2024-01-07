package io.github.aparx.perx.message;

import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 08:50
 * @since 1.0
 */
public class TestLookupPopulator {

  @Test
  public void testPutUsingString() {
    LookupPopulator populator = new LookupPopulator();
    populator.put(ArrayPath.of(), "__init");
    Assertions.assertEquals("__init", populator.getLookup().lookup(StringUtils.EMPTY));

    Assertions.assertNull(populator.getLookup().lookup("a.a"));
    populator.put(ArrayPath.of("a", "a"), "__init");
    Assertions.assertEquals("__init", populator.getLookup().lookup("a.a"));
    populator.put(ArrayPath.of("a", "a"), "fox");
    Assertions.assertEquals("fox", populator.getLookup().lookup("a.a"));
    Assertions.assertNull(populator.getLookup().lookup("a.b"));
    populator.put(ArrayPath.of("a", "b"), "ran");
    Assertions.assertEquals("ran", populator.getLookup().lookup("a.b"));
    Assertions.assertNull(populator.getLookup().lookup("a.c"));
    populator.put(ArrayPath.of("a", "c"), "over");
    Assertions.assertEquals("over", populator.getLookup().lookup("a.c"));
  }

  @Test
  public void testPutUsingSupplier() {
    LookupPopulator populator = new LookupPopulator();
    Assertions.assertNull(populator.getLookup().lookup("a.a"));
    populator.put(ArrayPath.of("a", "a"), () -> "__init");
    Assertions.assertEquals("__init", populator.getLookup().lookup("a.a"));
    populator.put(ArrayPath.of("a", "a"), () -> "fox");
    Assertions.assertEquals("fox", populator.getLookup().lookup("a.a"));
    Assertions.assertNull(populator.getLookup().lookup("a.b"));
    populator.put(ArrayPath.of("a", "b"), () -> "ran");
    Assertions.assertEquals("ran", populator.getLookup().lookup("a.b"));
    Assertions.assertNull(populator.getLookup().lookup("a.c"));
    populator.put(ArrayPath.of("a", "c"), () -> "over");
    Assertions.assertEquals("over", populator.getLookup().lookup("a.c"));

    AtomicInteger integer = new AtomicInteger();
    populator.put(ArrayPath.of("c"), () -> String.valueOf(integer.incrementAndGet()));
    Assertions.assertNotEquals(populator.getLookup().lookup("c"), populator.getLookup().lookup("c"));
  }

}
