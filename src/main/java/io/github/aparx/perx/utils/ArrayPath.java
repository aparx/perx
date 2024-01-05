package io.github.aparx.perx.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A specific location determined by an immutable array of segments that define a final path
 * which consists of all segments joined by an external (independent) separator.
 * <p>In previous versions of Bufig this implementation was named {@code ConfigPath}
 * representing a specific location within a configuration. This is still the
 * case, but this implementation is also very useful outside of configuration context.
 * <p>Every segment of a path is ensured to not be null and not blank.
 * <p>A string path can be parsed into a {@code ConfigPath} through the
 * {@link #parse(String, char)} method, or {@link #parseAdd(String, char)} to add a string
 * path to a current path object.
 *
 * @author aparx (Vinzent Z.)
 * @version 2023-11-20 14:43
 * @apiNote This code has been refactored from my library Bufig:
 * <a href="https://github.com/aparx/bufig-library">
 * https://github.com/aparx/bufig-library
 * </a>
 * @see #parse(String, char)
 * @see #parseAdd(String, char)
 * @see #join(char)
 * @see #isValidSegment(String)
 * @since 2.0
 */
public final class ArrayPath implements Iterable<@NonNull String> {

  public static final char DEFAULT_SEPARATOR = '.';

  private static final ArrayPath EMPTY = new ArrayPath(ArrayUtils.EMPTY_STRING_ARRAY);

  private final @NonNull String @NonNull [] segments;

  private final int hashCode;

  private final Supplier<String> defaultJoin = Suppliers.memoize(() -> join0(DEFAULT_SEPARATOR));

  public ArrayPath(@NonNull String @NonNull [] segments) {
    this.segments = segments;
    this.hashCode = Arrays.hashCode(segments);
  }

  public static boolean isValidSegment(String segment) {
    return StringUtils.isNotBlank(segment);
  }

  public static ArrayPath of(String @NonNull [] segments) {
    if (segments.length == 0)
      return EMPTY;
    if (segments.length == 1)
      return of(segments[0]);
    String[] array = Arrays.stream(segments)
        .filter(ArrayPath::isValidSegment)
        .toArray(String[]::new);
    return ArrayUtils.isEmpty(array) ? EMPTY : new ArrayPath(array);
  }

  public static ArrayPath of(String segment, String... successors) {
    return of(ArrayUtils.addFirst(successors, segment));
  }

  public static ArrayPath of(String segment) {
    if (!isValidSegment(segment)) return EMPTY;
    return new ArrayPath(new String[]{segment});
  }

  public static ArrayPath of() {
    return EMPTY;
  }

  /**
   * Parses given {@code path} string into a path object by splitting it up into segments
   * using the given {@code pathSeparator} as the delimiter. Any blank segment is ignored.
   *
   * @param path          the string path to parse
   * @param pathSeparator the path separator, used to split given {@code path}
   * @return the path object, which contains non-null non-blank segments in order of split
   * @see #of(String[])
   * @see #isValidSegment(String)
   * @see String#split(String)
   */
  public static ArrayPath parse(String path, char pathSeparator) {
    if (path.isEmpty())
      return EMPTY;
    int index = path.indexOf(pathSeparator);
    if (index == -1)
      return of(path);
    ArrayList<String> list = new ArrayList<>();
    do {
      String segment = path.substring(0, index);
      if (isValidSegment(segment)) list.add(segment);
      path = path.substring(1 + index);
    } while ((index = path.indexOf(pathSeparator)) != -1);
    if (isValidSegment(path)) list.add(path);
    else if (list.isEmpty()) return EMPTY;
    return new ArrayPath(list.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
  }

  /**
   * Parses {@code path} using the {@code DEFAULT_SEPARATOR}.
   *
   * @see #DEFAULT_SEPARATOR
   * @see #parse(String, char)
   * @since 2.1
   */
  public static ArrayPath parse(String path) {
    return parse(path, DEFAULT_SEPARATOR);
  }

  public int length() {
    return segments.length;
  }

  public boolean isEmpty() {
    return segments.length == 0;
  }

  /**
   * Joins all segments to a string using {@code pathSeparator}.
   * <p>This method is a kind of inverse to the {@code parse} method.
   *
   * @param pathSeparator the separator used to join all segments
   * @return a new string of all segments joined using {@code pathSeparator}
   * @see #join()
   * @see #parse(String, char)
   * @see #parseAdd(String, char)
   */
  public String join(char pathSeparator) {
    if (pathSeparator == DEFAULT_SEPARATOR)
      return defaultJoin.get();
    return join0(pathSeparator);
  }

  public String join() {
    return join(DEFAULT_SEPARATOR);
  }

  public @NonNull String first() {
    return get(0);
  }

  public @NonNull String last() {
    return get(length() - 1);
  }

  public @NonNull ArrayPath parent() {
    int n = length();
    if (n < 2)
      return ArrayPath.of();
    return subpath(0, n - 1);
  }

  public boolean hasParent() {
    return length() > 1;
  }

  private String join0(char pathSeparator) {
    final int n = length();
    // for n = [0, 1] use faster string concatenation
    if (n == 0)
      return StringUtils.EMPTY;
    if (n == 1)
      return segments[0];
    // for n = [2, ) use builder
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < n; ++i) {
      if (i != 0) builder.append(pathSeparator);
      builder.append(segments[i]);
    }
    return builder.toString();
  }

  public @NonNull String get(@NonNegative int index) {
    Preconditions.checkElementIndex(index, length());
    return Objects.requireNonNull(segments[index]);
  }

  @CheckReturnValue
  public @NonNull ArrayPath set(@NonNegative int index, String segment) {
    Preconditions.checkElementIndex(index, length());
    if (!isValidSegment(segment))
      return new ArrayPath(ArrayUtils.remove(segments, index));
    @NonNull String[] array = toArray();
    array[index] = segment;
    return new ArrayPath(array);
  }

  @CheckReturnValue
  public @NonNull ArrayPath add(@NonNull ArrayPath other) {
    if (other.isEmpty()) return this;
    if (isEmpty()) return other;
    return new ArrayPath(ArrayUtils.addAll(segments, other.segments));
  }

  @CheckReturnValue
  public @NonNull ArrayPath add(String @NonNull [] other) {
    if (ArrayUtils.isEmpty(other)) return this;
    if (isEmpty()) return of(other);
    return of(ArrayUtils.addAll(segments, other));
  }

  @CheckReturnValue
  public @NonNull ArrayPath add(String segment) {
    if (isEmpty()) return of(segment);
    if (!isValidSegment(segment)) return this;
    return new ArrayPath(ArrayUtils.add(segments, segment));
  }

  @CheckReturnValue
  public @NonNull ArrayPath add(String nextSegment, String... successors) {
    if (isEmpty()) return of(nextSegment, successors);
    if (!isValidSegment(nextSegment)
        && ArrayUtils.isEmpty(successors))
      return this;
    return add(ArrayPath.of(nextSegment, successors));
  }

  @CheckReturnValue
  public @NonNull ArrayPath parseAdd(String join, char pathSeparator) {
    return add(ArrayPath.parse(join, pathSeparator));
  }

  @CheckReturnValue
  public @NonNull ArrayPath parseAdd(String join) {
    return parseAdd(join, DEFAULT_SEPARATOR);
  }

  @CheckReturnValue
  public ArrayPath subpath(int startInclusiveIndex, int stopExclusiveIndex) {
    Preconditions.checkElementIndex(startInclusiveIndex, length());
    Preconditions.checkPositionIndex(stopExclusiveIndex, length());
    Preconditions.checkState(startInclusiveIndex <= stopExclusiveIndex,
        "startInclusiveIndex must be less or equal to stopExclusiveIndex");
    if (startInclusiveIndex == stopExclusiveIndex)
      return EMPTY;
    return new ArrayPath(Arrays.copyOfRange(segments, startInclusiveIndex,
        stopExclusiveIndex));
  }

  public ArrayPath subpath(int startInclusiveIndex) {
    return subpath(startInclusiveIndex, length());
  }

  public @NonNull Stream<@NonNull String> stream() {
    return Arrays.stream(segments);
  }

  public @NonNull String @NonNull [] toArray() {
    return ArrayUtils.clone(segments);
  }

  @Override
  public @NonNull Iterator<@NonNull String> iterator() {
    return new Iterator<>() {
      int cursor = 0;

      @Override
      public boolean hasNext() {
        return cursor < length();
      }

      @Override
      public @NonNull String next() {
        return get(cursor++);
      }
    };
  }

  @Override
  public String toString() {
    return Arrays.toString(segments);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ArrayPath strings = (ArrayPath) o;
    if (strings.hashCode == hashCode)
      return true;
    return Arrays.equals(segments, strings.segments);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
