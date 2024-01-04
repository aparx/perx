package io.github.aparx.perx.utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2023-12-04 07:12
 * @apiNote Copied over from Bufig (my library)
 * @since 1.0
 */
public class WeakHashSet<E> extends AbstractSet<E> {

  private static final int DEFAULT_INITIAL_CAPACITY = 10;

  private static final Object VALUE = new Object();

  protected final WeakHashMap<E, Object> internalMap;

  public WeakHashSet() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  public WeakHashSet(int initialCapacity) {
    this.internalMap = new WeakHashMap<>(initialCapacity);
  }

  public WeakHashSet(int initialCapacity, float loadFactor) {
    this.internalMap = new WeakHashMap<>(initialCapacity, loadFactor);
  }

  public WeakHashSet(@NonNull Collection<? extends E> initialElements) {
    this(initialElements.size());
    addAll(initialElements);
  }

  @Override
  public int size() {
    return internalMap.size();
  }

  @Override
  public boolean add(E e) {
    return internalMap.putIfAbsent(e, VALUE) == null;
  }

  @Override
  public boolean remove(Object o) {
    return internalMap.remove(o) == VALUE;
  }

  @Override
  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean contains(Object o) {
    if (o == VALUE) return true;
    return internalMap.get(o) == VALUE;
  }

  @Override
  public @NonNull Iterator<E> iterator() {
    return internalMap.keySet().iterator();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WeakHashSet<?> that = (WeakHashSet<?>) o;
    return Objects.equals(internalMap, that.internalMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(internalMap);
  }

  @Override
  public String toString() {
    return "WeakHashSet{" + internalMap.keySet() + '}';
  }
}
