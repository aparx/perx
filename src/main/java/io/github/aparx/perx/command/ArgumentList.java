package io.github.aparx.perx.command;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 10:29
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class ArgumentList implements Iterable<CommandArgument> {

  public static final char ARGUMENT_SEPARATOR = ' ';

  private static final ArgumentList EMPTY =
      new ArgumentList(ArrayPath.of(), new CommandArgument[0]);

  private final ArrayPath args;
  private final @Nullable CommandArgument[] compiled;

  private ArgumentList(ArrayPath args, @Nullable CommandArgument[] compiled) {
    Preconditions.checkArgument(compiled.length == args.length(), "Length mismatch");
    this.args = args;
    this.compiled = compiled;
  }

  public static ArgumentList of(ArrayPath args) {
    if (!args.isEmpty())
      return new ArgumentList(args, new CommandArgument[args.length()]);
    return EMPTY;
  }

  public static ArgumentList parse(String line) {
    return of(ArrayPath.parse(line, ArrayPath.DEFAULT_SEPARATOR));
  }

  public static ArgumentList of() {
    return EMPTY;
  }

  public int length() {
    return args.length();
  }

  public boolean isEmpty() {
    return args.isEmpty();
  }

  public String getString(int index) {
    return args.get(index);
  }

  public CommandArgument get(int index) {
    Preconditions.checkElementIndex(index, compiled.length);
    @Nullable CommandArgument arg = compiled[index];
    if (arg != null) return arg;
    synchronized (this) {
      arg = compiled[index];
      if (arg != null) return arg;
      arg = new CommandArgument(args.get(index));
      compiled[index] = arg;
      return arg;
    }
  }

  public ArgumentList sublist(int fromInclusiveIndex) {
    return sublist(fromInclusiveIndex, length());
  }

  public ArgumentList sublist(int fromInclusiveIndex, int toExclusiveIndex) {
    Preconditions.checkElementIndex(fromInclusiveIndex, length());
    Preconditions.checkPositionIndex(toExclusiveIndex, length());
    return new ArgumentList(
        args.subpath(fromInclusiveIndex, toExclusiveIndex),
        Arrays.copyOfRange(compiled, fromInclusiveIndex, toExclusiveIndex));
  }

  public String join(int fromInclusiveIndex, char separator) {
    return args.subpath(fromInclusiveIndex).join(separator);
  }

  public String join(char separator) {
    return args.join(separator);
  }

  public String join() {
    return args.join(ARGUMENT_SEPARATOR);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    ArgumentList that = (ArgumentList) object;
    return Objects.equals(args, that.args);
  }

  @Override
  public int hashCode() {
    return Objects.hash(args);
  }

  @Override
  public Iterator<CommandArgument> iterator() {
    return new Iterator<>() {
      int cursor = 0;

      @Override
      public boolean hasNext() {
        return cursor < length();
      }

      @Override
      public CommandArgument next() {
        return get(cursor++);
      }
    };
  }
}
