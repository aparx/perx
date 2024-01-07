package io.github.aparx.perx.message;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 08:38
 * @since 1.0
 */
public class TestLocalizedMessage {

  @Test
  public void testGetRawMessage() {
    testRawMessage("hello");
    testRawMessage("world");
    testRawMessage("a");
    testRawMessage("b");
    testRawMessage("c");
  }

  @Test
  public void testGetMessage() {
    testColoredMessage(ChatColor.RED + "hello");
    testColoredMessage("&aworld");
    testColoredMessage("a&a");
    testColoredMessage("&bb");
    testColoredMessage("&clorem");
    testColoredMessage("&cip&osum");
  }

  @Test
  public void toLines() {
    testLines(List.of());
    testLines(List.of("a", "b"));
    testLines(List.of("c", "d"));
    testLines(List.of("c", "d", "e", "f"));
    testLines(List.of("hello", "world"));
  }

  @Test
  public void extend() {
    LocalizedMessage message = LocalizedMessage.of("hello {var0} world {var1}");
    Assertions.assertEquals(message.getMessage(), message.substitute());
    StringSubstitutor sub = message.extend(createLookup(Map.of("var0", "a", "var1", "b")));
    Assertions.assertEquals(message.getMessage(), message.substitute());
    Assertions.assertEquals("hello a world b", message.substitute(sub));

    message = LocalizedMessage.of("hello {a} {b} {c}", createLookup(Map.of("a", "A")));
    Assertions.assertEquals("hello A {b} {c}", message.substitute());
    sub = message.extend(createLookup(Map.of("a", "a", "b", "B", "c", "C")));
    Assertions.assertEquals("hello a B C", message.substitute(sub));
    sub = message.extend(createLookup(Map.of("b", "B", "c", "C")));
    Assertions.assertEquals("hello A B C", message.substitute(sub));
  }

  @Test
  public void substitute() {
    LocalizedMessage message = LocalizedMessage.of("hello {var0} world {var1}");
    Assertions.assertEquals(message.getMessage(), message.substitute());
    Assertions.assertEquals("hello a world {var1}", message.substitute(Map.of("var0", "a")));
    Assertions.assertEquals("hello {var0} world {var1}", message.substitute(createLookup(Map.of())));
    Assertions.assertEquals("hello {var0} world v1", message.substitute(createLookup(Map.of("var1", "v1"))));
  }

  @Test
  public void substituteArgs() {
    LocalizedMessage message = LocalizedMessage.of("hello {0}, {1}, {2}, {3}");
    Assertions.assertEquals(message.getRawMessage(), message.substituteArgs());
    Assertions.assertEquals("hello a, {1}, {2}, {3}", message.substituteArgs("a"));
    Assertions.assertEquals("hello a, b, {2}, {3}", message.substituteArgs("a", "b"));
    Assertions.assertEquals("hello a, true, c, {3}", message.substituteArgs("a", true, "c"));
    Assertions.assertEquals("hello a, b, c, 4", message.substituteArgs("a", "b", "c", 4));
    Assertions.assertEquals("hello a, b, c, 4", message.substituteArgs("a", "b", "c", 4, 5));
    Assertions.assertEquals("hello {0}, b, c, 4", message.substituteArgs(null, "b", "c", 4, 5));
    Assertions.assertEquals("hello {0}, b, {2}, d", message.substituteArgs(null, "b", null, "d"));
  }

  private static StringLookup createLookup(Map<String, ?> map) {
    return StringLookupFactory.INSTANCE.mapStringLookup(map);
  }

  private static void testLines(Collection<String> collection) {
    Assertions.assertArrayEquals(collection.toArray(new String[0]), LocalizedMessage.of(collection).toLines());
  }

  private static void testRawMessage(String message) {
    Assertions.assertEquals(message, LocalizedMessage.of(message).getRawMessage());
  }

  private static void testColoredMessage(String message) {
    Assertions.assertEquals(ChatColor.translateAlternateColorCodes('&', message),
        LocalizedMessage.of(message).getMessage());
  }

}
