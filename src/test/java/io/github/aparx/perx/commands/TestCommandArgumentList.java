package io.github.aparx.perx.commands;

import io.github.aparx.perx.command.args.CommandArgumentList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-18 01:38
 * @since 1.0
 */
public class TestCommandArgumentList {

  @Test
  public void testToArray() {
    CommandArgumentList arglist = CommandArgumentList.of("a", "b", "c", "d", "e");
    Assertions.assertArrayEquals(new String[]{"a", "b", "c", "d", "e"}, arglist.toArray());
  }

  @Test
  public void testSublist() {
    CommandArgumentList args0 = CommandArgumentList.of("a", "b", "c", "d", "e");
    Assertions.assertArrayEquals(new String[0], args0.sublist(1, 1).toArray());
    Assertions.assertArrayEquals(new String[]{"b"}, args0.sublist(1, 2).toArray());
    Assertions.assertArrayEquals(new String[]{"b", "c"}, args0.sublist(1, 3).toArray());
    Assertions.assertArrayEquals(new String[]{"c"}, args0.sublist(2, 3).toArray());

    CommandArgumentList args1 = args0.sublist(1, 4);
    Assertions.assertArrayEquals(new String[]{"b", "c", "d"}, args1.toArray());
    Assertions.assertArrayEquals(new String[]{"c", "d"}, args1.sublist(1).toArray());
    Assertions.assertArrayEquals(new String[]{"c"}, args1.sublist(1, 2).toArray());
    Assertions.assertArrayEquals(new String[0], args1.sublist(2, 2).toArray());
    Assertions.assertThrows(IllegalArgumentException.class, () -> args1.sublist(3, 2).toArray());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args1.sublist(5, 5).toArray());
  }

  @Test
  public void testGetString() {
    CommandArgumentList args0 = CommandArgumentList.of("a", "b", "c");
    Assertions.assertEquals("a", args0.getString(0));
    Assertions.assertEquals("b", args0.getString(1));
    Assertions.assertEquals("c", args0.getString(2));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args0.getString(3));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args0.getString(-1));

    CommandArgumentList args1 = args0.sublist(1);
    Assertions.assertEquals("b", args1.getString(0));
    Assertions.assertEquals("c", args1.getString(1));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args1.getString(2));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args1.getString(-1));
  }

  @Test
  public void testGet() {
    CommandArgumentList args0 = CommandArgumentList.of("a", "b", "c");
    Assertions.assertEquals("a", args0.get(0).value());
    Assertions.assertEquals("b", args0.get(1).value());
    Assertions.assertEquals("c", args0.get(2).value());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args0.get(3));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args0.get(-1));

    CommandArgumentList args1 = args0.sublist(1);
    Assertions.assertEquals("b", args1.get(0).value());
    Assertions.assertEquals("c", args1.get(1).value());
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args1.get(2));
    Assertions.assertThrows(IndexOutOfBoundsException.class, () -> args1.get(-1));
  }

}
