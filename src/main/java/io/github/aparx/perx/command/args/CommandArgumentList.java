package io.github.aparx.perx.command.args;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
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
      new CommandArgumentList(new String[0], new CommandArgument[0]);

  private final int hashCode;
  private final String[] args;
  private final @Nullable CommandArgument[] compiled;

  private CommandArgumentList(String[] args, @Nullable CommandArgument[] compiled) {
    Preconditions.checkArgument(compiled.length == args.length, "Length mismatch");
    this.args = args;
    this.compiled = compiled;
    this.hashCode = Arrays.hashCode(args);
  }

  public static CommandArgumentList of(String[] args) {
    return new CommandArgumentList(args, new CommandArgument[args.length]);
  }

  public static CommandArgumentList parse(String line) {
    return of(line.split(ARGUMENT_SEPARATOR));
  }

  public static CommandArgumentList of() {
    return EMPTY;
  }

  public int length() {
    return args.length;
  }

  public boolean isEmpty() {
    return ArrayUtils.isEmpty(args);
  }

  public String getString(int index) {
    Preconditions.checkElementIndex(index, args.length);
    return args[index];
  }

  public CommandArgument first() {
    return get(0);
  }

  public CommandArgument get(int index) {
    Preconditions.checkElementIndex(index, compiled.length);
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

  public CommandArgumentList sublist(int fromInclusiveIndex, int toExclusiveIndex) {
    Preconditions.checkPositionIndex(fromInclusiveIndex, length());
    Preconditions.checkPositionIndex(toExclusiveIndex, length());
    return new CommandArgumentList(
        Arrays.copyOfRange(args, fromInclusiveIndex, toExclusiveIndex),
        Arrays.copyOfRange(compiled, fromInclusiveIndex, toExclusiveIndex));
  }

  public String join(int fromInclusiveIndex, CharSequence separator) {
    if (fromInclusiveIndex == 0)
      return String.join(separator, args);
    StringBuilder builder = new StringBuilder();
    for (String arg : args) {
      if (!builder.isEmpty()) builder.append(separator);
      builder.append(arg);
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
        return cursor < length();
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
