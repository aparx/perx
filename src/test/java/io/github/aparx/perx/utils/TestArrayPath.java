package io.github.aparx.perx.utils;

import io.github.aparx.perx.utils.ArrayPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Copied from Bufig.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-11-23 22:23
 * @since 1.0
 */
public class TestArrayPath {

  @Test
  public void of() {
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.of(new String[]{"a", "b", "c"}).toArray());
    Assertions.assertArrayEquals(new String[0], ArrayPath.of(new String[0]).toArray());
    Assertions.assertArrayEquals(new String[]{"a"}, ArrayPath.of("a").toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b"}, ArrayPath.of("a", "b").toArray());
    Assertions.assertArrayEquals(new String[0], ArrayPath.of().toArray());

    Assertions.assertArrayEquals(new String[]{"a", "b"},
        ArrayPath.of("a", " ", "b").toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.of("a", " ", "b", "c", " ".repeat(3)).toArray());
  }

  @Test
  public void parse() {
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.parse("a.b.c", '.').toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.parse("a..b.c", '.').toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.parse("..a..b.c", '.').toArray());
    Assertions.assertArrayEquals(new String[]{"a"},
        ArrayPath.parse("a", '.').toArray());
    Assertions.assertArrayEquals(new String[0],
        ArrayPath.parse("", '.').toArray());
    Assertions.assertArrayEquals(new String[]{"b", "c"},
        ArrayPath.parse("b.c", '.').toArray());
    Assertions.assertArrayEquals(new String[]{"b.c"},
        ArrayPath.parse("b.c", ',').toArray());
    Assertions.assertArrayEquals(new String[]{"b.c", "d.e"},
        ArrayPath.parse("b.c,d.e", ',').toArray());
  }

  @Test
  public void get() {
    Assertions.assertEquals("d", ArrayPath.of("a", "b", "c", "d").get(3));
    Assertions.assertEquals("a", ArrayPath.of("a", "b").get(0));
    Assertions.assertEquals("b", ArrayPath.of("a", "b").get(1));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> ArrayPath.of("a", "b").get(3));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> ArrayPath.of("a", "b").get(-1));
    Assertions.assertThrows(IndexOutOfBoundsException.class,
        () -> ArrayPath.of().get(1));
  }

  @Test
  public void join() {
    Assertions.assertEquals("a.b.c", ArrayPath.of("a", "b", "c").join('.'));
    Assertions.assertEquals("a,b,c", ArrayPath.of("a", "b", "c").join(','));
    Assertions.assertEquals("hox.a.b.c", ArrayPath.of("hox.a", "b", "c").join('.'));
    Assertions.assertEquals("a", ArrayPath.of("a").join('.'));
    Assertions.assertEquals("", ArrayPath.of().join('.'));
  }

  @Test
  public void add() {
    // add(ConfigPath)
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.of("a", "b").add(ArrayPath.of("c")).toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b"},
        ArrayPath.of("a", "b").add(ArrayPath.of()).toArray());
    Assertions.assertArrayEquals(new String[]{"a", "a", "b"},
        ArrayPath.of("a").add(ArrayPath.of("a", "b")).toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b"},
        ArrayPath.of("a").add(ArrayPath.of("b")).toArray());
    Assertions.assertArrayEquals(new String[]{"a"},
        ArrayPath.of().add(ArrayPath.of("a")).toArray());

    // add(String) / add(String[])
    Assertions.assertArrayEquals(new String[]{"a", "b", "c"},
        ArrayPath.of("a", "b").add("c").toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b"},
        ArrayPath.of("a", "b").add("").toArray());
    Assertions.assertArrayEquals(new String[]{"a", "a", "b"},
        ArrayPath.of("a").add(new String[]{"a", "b"}).toArray());
    Assertions.assertArrayEquals(new String[]{"a", "b"},
        ArrayPath.of("a").add(new String[]{"b"}).toArray());
    Assertions.assertArrayEquals(new String[]{"a"},
        ArrayPath.of().add(new String[]{"a"}).toArray());
  }

  @Test
  public void subpath() {
    ArrayPath path = ArrayPath.of("a", "b", "c", "d", "e", "f");
    Assertions.assertArrayEquals(new String[]{"a", "b"},
        path.subpath(0, 2).toArray());
    Assertions.assertArrayEquals(new String[]{"b"},
        path.subpath(1, 2).toArray());
    Assertions.assertArrayEquals(new String[0],
        path.subpath(2, 2).toArray());
    Assertions.assertArrayEquals(new String[]{"d", "e"},
        path.subpath(3, 5).toArray());
    Assertions.assertArrayEquals(path.toArray(),
        path.subpath(0, path.length()).toArray());
  }

  @Test
  public void parent() {
    Assertions.assertEquals(ArrayPath.of("a", "b").parent(), ArrayPath.of("a"));
    Assertions.assertEquals(ArrayPath.of("a").parent(), ArrayPath.of());
  }


}
