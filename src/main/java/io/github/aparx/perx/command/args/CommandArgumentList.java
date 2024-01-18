package io.github.aparx.perx.command.args;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 10:29
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class CommandArgumentList implements Iterable<CommandArgument> {

  public static final String ARGUMENT_SEPARATOR = " ";

  private static final CommandArgumentList EMPTY =
      new CommandArgumentList(ArrayUtils.EMPTY_STRING_ARRAY);

  private final int hashCode;
  private final String[] args;
  private final @Nullable CommandArgument[] compiled;

  private final int offset, length;

  private CommandArgumentList(String[] args) {
    this(args, new CommandArgument[args.length], 0, args.length);
  }

  private CommandArgumentList(
      String[] args, @Nullable CommandArgument[] compiled, int offset, int length) {
    Preconditions.checkArgument(compiled.length == args.length, "Length mismatch");
    assert offset >= 0 && offset <= args.length;
    assert length >= 0 && length <= args.length;
    this.args = args;
    this.compiled = compiled;
    this.hashCode = hashCode(args, offset, length);
    this.offset = offset;
    this.length = length;
  }

  public static CommandArgumentList of(String... args) {
    Preconditions.checkNotNull(args, "Vararg must not be null");
    Validate.noNullElements(args, "Argument must not be null");
    return new CommandArgumentList(args);
  }

  public static CommandArgumentList of() {
    return EMPTY;
  }

  public static CommandArgumentList parse(String line) {
    return of(line.split(ARGUMENT_SEPARATOR));
  }

  private static int hashCode(String[] args, int offset, int length) {
    int hashCode = 0;
    for (int i = 0, j; i < length && (j = offset + i) < args.length; ++i)
      hashCode = 31 * hashCode + args[j].hashCode();
    return hashCode;
  }

  public int length() {
    return length;
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public String getString(int index) {
    Preconditions.checkElementIndex(index, length);
    return args[offset + index];
  }

  public CommandArgument first() {
    return get(0);
  }

  public CommandArgument get(int index) {
    Preconditions.checkElementIndex(index, length);
    index += offset;
    @Nullable CommandArgument arg = compiled[index];
    if (arg != null) return arg;
    synchronized (this) {
      arg = compiled[index];
      if (arg != null) return arg;
      arg = new CommandArgument(args[index]);
      compiled[index] = arg;
      return arg;
    }
  }

  public CommandArgumentList skip() {
    return isEmpty() ? this : sublist(1);
  }

  public CommandArgumentList sublist(int fromInclusiveIndex) {
    return sublist(fromInclusiveIndex, length());
  }

  public CommandArgumentList sublist(int fromInclusive, int toExclusive) {
    Preconditions.checkPositionIndex(fromInclusive, length());
    Preconditions.checkPositionIndex(toExclusive, length());
    Preconditions.checkArgument(toExclusive >= fromInclusive, "from > to");
    if (fromInclusive == toExclusive) return EMPTY;
    return new CommandArgumentList(args, compiled,
        offset + fromInclusive, (toExclusive - fromInclusive));
  }

  public String join(int fromInclusiveIndex, CharSequence separator) {
    StringBuilder builder = new StringBuilder();
    for (int i = fromInclusiveIndex; i < length; ++i) {
      if (!builder.isEmpty()) builder.append(separator);
      builder.append(getString(i));
    }
    return builder.toString();
  }

  public String join(CharSequence separator) {
    return String.join(separator, args);
  }

  public String join(int fromInclusiveIndex) {
    return join(fromInclusiveIndex, ARGUMENT_SEPARATOR);
  }

  public String join() {
    return join(ARGUMENT_SEPARATOR);
  }

  public String[] toArray() {
    if (offset == 0 && length == args.length)
      return args.clone();
    return Arrays.copyOfRange(args, offset, offset + length);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    CommandArgumentList that = (CommandArgumentList) object;
    return Arrays.equals(args, that.args);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public Iterator<CommandArgument> iterator() {
    return new Iterator<>() {
      int cursor = 0;

      @Override
      public boolean hasNext() {
        return cursor < length;
      }

      @Override
      public CommandArgument next() {
        return get(cursor++);
      }
    };
  }

  @Override
  public String toString() {
    return "CommandArgs{" +
        "args=" + Arrays.toString(args) +
        '}';
  }
}
