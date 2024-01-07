package io.github.aparx.perx.command;

import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.message.MessageRepository;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 03:14
 * @since 1.0
 */
public final class CommandAssertion {

  private CommandAssertion() {
    throw new AssertionError();
  }

  public static void checkTrue(boolean condition) throws CommandError {
    checkTrue(condition, condition + " but expected true");
  }

  public static void checkTrue(
      boolean condition,
      @Nullable String message
  ) throws CommandError {
    if (!condition) throw new CommandError(message);
  }

  public static void checkTrue(
      boolean condition,
      @Nullable Function<MessageRepository, String> messageFactory
  ) throws CommandError {
    if (!condition) throw new CommandError(messageFactory);
  }

  public static void checkFalse(boolean condition) throws CommandError {
    checkFalse(condition, condition + " but expected false");
  }

  public static void checkFalse(
      boolean condition,
      @Nullable String message
  ) throws CommandError {
    if (condition) throw new CommandError(message);
  }

  public static void checkFalse(
      boolean condition,
      @Nullable Function<MessageRepository, String> messageFactory
  ) throws CommandError {
    if (condition) throw new CommandError(messageFactory);
  }

  public static void checkIsPlayer(CommandContext context) throws CommandError {
    checkIsPlayer(context, (register) -> Message.ERROR_PLAYER.substitute(
        register, new LookupPopulator().put(ArrayPath.of(), context).getLookup()
    ));
  }

  public static void checkIsPlayer(
      CommandContext context,
      @Nullable Function<MessageRepository, String> messageFactory
  ) throws CommandError {
    if (!context.isPlayer())
      throw new CommandError(messageFactory);
  }

  public static void checkIsPlayer(
      CommandContext context,
      @Nullable String message
  ) throws CommandError {
    if (!context.isPlayer())
      throw new CommandError(message);
  }

  public static void checkInRange(int number, int inclusiveMin, int inclusiveMax) throws CommandError {
    if (number < inclusiveMin || number > inclusiveMax)
      throw new CommandError((x) ->
          Message.ERROR_NUMBER_RANGE.substitute(x, new LookupPopulator()
              .put(ArrayPath.of("min"), String.valueOf(inclusiveMin))
              .put(ArrayPath.of("max"), String.valueOf(inclusiveMax))
              .getLookup()));
  }

}
