package io.github.aparx.perx.utils.pagination;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 06:37
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class BasicPageContainer<P extends Collection<?>> implements Iterable<P> {

  private final List<P> pages = new ArrayList<>();

  public int size() {
    return pages.size();
  }

  public boolean isEmpty() {
    return pages.isEmpty();
  }

  public P getPage(int index) {
    return pages.get(index);
  }

  @CanIgnoreReturnValue
  public boolean addPage(P page) {
    Preconditions.checkNotNull(page, "Page must not be null");
    return pages.add(page);
  }

  @CanIgnoreReturnValue
  public boolean removePage(P page) {
    return pages.remove(page);
  }

  @CanIgnoreReturnValue
  public @Nullable P removePage(int index) {
    return pages.remove(index);
  }

  @Override
  public Iterator<P> iterator() {
    return pages.iterator();
  }
}
