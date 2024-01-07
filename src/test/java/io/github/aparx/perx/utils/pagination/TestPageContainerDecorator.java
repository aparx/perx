package io.github.aparx.perx.utils.pagination;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 08:06
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class TestPageContainerDecorator {

  @Test
  public void testAddElementAndAddElementsWithHashSet() {
    PageContainerDecorator<String, List<String>> decorator =
        PageContainerDecorator.of(3, new HashSet<>());
    Assertions.assertEquals(3, decorator.getMaxPerPage());
    Assertions.assertTrue(decorator.addElement("hello"));
    Assertions.assertFalse(decorator.addElement("hello"));
    Assertions.assertTrue(decorator.addElement("world"));
    Assertions.assertFalse(decorator.addElement("world"));
    Assertions.assertFalse(decorator.addElements(List.of("hello", "world")));
    Assertions.assertTrue(decorator.addElements(List.of("hello", "world", "x")));
    Assertions.assertTrue(decorator.addElements(List.of("y")));
    Assertions.assertEquals(4, decorator.elementCount());
    // expect 2 pages [["hello", "world", "x"], ["y"]]
    Assertions.assertEquals(2, decorator.getContainer().size());
    Assertions.assertEquals(List.of("hello", "world", "x"), decorator.getContainer().getPage(0));
    Assertions.assertEquals(List.of("y"), decorator.getContainer().getPage(1));
  }

  @Test
  public void testAddElementAndAddElements() {
    PageContainerDecorator<String, List<String>> decorator = PageContainerDecorator.of(1);
    Assertions.assertTrue(decorator.addElement("a"));
    Assertions.assertTrue(decorator.addElement("a"));
    Assertions.assertTrue(decorator.addElement("b"));
    Assertions.assertTrue(decorator.addElement("c"));
    Assertions.assertEquals(decorator.getContainer().size(), decorator.elementCount());
    Assertions.assertEquals(List.of("a"), decorator.getContainer().getPage(0));
    Assertions.assertEquals(List.of("a"), decorator.getContainer().getPage(1));
    Assertions.assertEquals(List.of("b"), decorator.getContainer().getPage(2));
    Assertions.assertEquals(List.of("c"), decorator.getContainer().getPage(3));

    Assertions.assertThrows(IllegalArgumentException.class, () -> PageContainerDecorator.of(0));
    Assertions.assertThrows(IllegalArgumentException.class, () -> PageContainerDecorator.of(-1));
  }

}
