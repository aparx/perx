package io.github.aparx.perx.message;

import io.github.aparx.perx.utils.ArrayPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 08:31
 * @since 1.0
 */
public class TestMessageMap {

  @Test
  public void testSet() {
    MessageMap map = new MessageMap();
    LocalizedMessage first = LocalizedMessage.of("hello");
    Assertions.assertNull(map.set(ArrayPath.of("a", "b"), first));
    Assertions.assertEquals(first, map.set(ArrayPath.of("a", "b"), LocalizedMessage.of("world")));
    Assertions.assertNull(map.set(ArrayPath.of(), LocalizedMessage.of("a")));
  }

  @Test
  public void testGet() {
    MessageMap map = new MessageMap();
    Assertions.assertNull(map.set(ArrayPath.of("a"), LocalizedMessage.of("a")));
    Assertions.assertNull(map.set(ArrayPath.of("b"), LocalizedMessage.of("b")));
    Assertions.assertNull(map.set(ArrayPath.of("b", "c"), LocalizedMessage.of("bc")));
    Assertions.assertNull(map.set(ArrayPath.of("b", "c", "d"), LocalizedMessage.of("bcd")));
    Assertions.assertEquals(LocalizedMessage.of("a"), map.get(ArrayPath.of("a")));
    Assertions.assertEquals(LocalizedMessage.of("b"), map.get(ArrayPath.of("b")));
    Assertions.assertEquals(LocalizedMessage.of("bc"), map.get(ArrayPath.of("b", "c")));
    Assertions.assertEquals(LocalizedMessage.of("bcd"), map.get(ArrayPath.of("b", "c", "d")));
    // not found tests
    Assertions.assertEquals(LocalizedMessage.of("[not, found]"), map.get(ArrayPath.of("not", "found")));
    Assertions.assertEquals(LocalizedMessage.of("[bc]"), map.get(ArrayPath.of("bc")));
    Assertions.assertEquals(LocalizedMessage.of("[bcd]"), map.get(ArrayPath.of("bcd")));
    // string tests
    Assertions.assertEquals(LocalizedMessage.of("a"), map.get("a"));
    Assertions.assertEquals(LocalizedMessage.of("b"), map.get("b"));
    Assertions.assertEquals(LocalizedMessage.of("bc"), map.get("b.c"));
    Assertions.assertEquals(LocalizedMessage.of("bcd"), map.get("b.c.d"));
    // not found tests
    Assertions.assertEquals(LocalizedMessage.of("[b, c, d, e]"), map.get("b.c.d.e"));
    Assertions.assertEquals(LocalizedMessage.of("[]"), map.get(ArrayPath.of()));
    Assertions.assertNull(map.set(ArrayPath.of(), LocalizedMessage.of("NIL")));
    Assertions.assertEquals(LocalizedMessage.of("NIL"), map.get(ArrayPath.of()));
  }

}
