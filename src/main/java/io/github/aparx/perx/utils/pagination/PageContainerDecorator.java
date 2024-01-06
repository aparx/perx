package io.github.aparx.perx.utils.pagination;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 06:43
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PageContainerDecorator<E, P extends Collection<? super E>>
    implements Iterable<E> {

  public static final Supplier<ArrayList<?>> DEFAULT_PAGE_FACTORY = ArrayList::new;

  private final BasicPageContainer<P> container;
  private final Collection<@Nullable E> elements;
  private final @NonNegative int maxPerPage;

  private final Supplier<P> pageFactory;

  protected PageContainerDecorator(
      BasicPageContainer<P> container,
      @NonNegative int maxPerPage,
      Supplier<P> pageFactory) {
    this(container, new ArrayList<>(), maxPerPage, pageFactory);
  }

  protected PageContainerDecorator(
      BasicPageContainer<P> container,
      Collection<@Nullable E> collection,
      @NonNegative int maxPerPage,
      Supplier<P> pageFactory) {
    Preconditions.checkNotNull(container, "Paginator must not be null");
    Preconditions.checkNotNull(collection, "Collection must not be null");
    Preconditions.checkArgument(maxPerPage > 1, "Must be more than one");
    Preconditions.checkNotNull(pageFactory, "Factory must not be null");
    this.container = container;
    this.maxPerPage = maxPerPage;
    this.elements = collection;
    this.pageFactory = pageFactory;
  }

  @SuppressWarnings("NullableProblems")
  public static <E, P extends Collection<? super E>>
  PageContainerDecorator<E, P> of(
      BasicPageContainer<P> container,
      Collection<@Nullable E> collection,
      @NonNegative int maxPerPage,
      Supplier<P> pageFactory) {
    return new PageContainerDecorator<>(container, collection, maxPerPage, pageFactory);
  }

  public static <E, P extends Collection<? super E>>
  PageContainerDecorator<E, P> of(@NonNegative int maxPerPage, Supplier<P> pageFactory) {
    return of(new BasicPageContainer<>(), new ArrayList<>(), maxPerPage, pageFactory);
  }

  @SuppressWarnings({"unchecked", "rawtypes"}) // OK
  public static <E> PageContainerDecorator<E, List<E>> of(@NonNegative int maxPerPage) {
    return of(maxPerPage, (Supplier<List<E>>) (Supplier) DEFAULT_PAGE_FACTORY);
  }

  public BasicPageContainer<P> getContainer() {
    return container;
  }

  public int getMaxPerPage() {
    return maxPerPage;
  }

  public Supplier<P> getPageFactory() {
    return pageFactory;
  }

  @CanIgnoreReturnValue
  public boolean addElement(@Nullable E element) {
    if (!elements.add(element))
      return false;
    push(Collections.singletonList(element));
    return true;
  }

  @CanIgnoreReturnValue
  public boolean addElements(Collection<E> elements) {
    if (!this.elements.addAll(elements))
      return false;
    push(elements);
    return true;
  }

  /** Pushes {@code elements} to the current stack of pages and may create new pages */
  protected void push(Collection<@Nullable E> elements) {
    int maxPerPage = getMaxPerPage();
    for (E e : elements) {
      int pageIndex = container.size() - 1;
      @Nullable P target = (pageIndex < 0 ? null : container.getPage(pageIndex));
      if (target == null || target.size() >= maxPerPage)
        container.addPage(target = pageFactory.get());
      target.add(e);
    }
  }

  @Override
  public Iterator<E> iterator() {
    // we delegate the iterator to delete the ability to remove elements
    Iterator<@Nullable E> iterator = elements.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public @Nullable E next() {
        return iterator.next();
      }
    };
  }
}
