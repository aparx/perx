package io.github.aparx.perx.command.errors;

import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.message.MessageRegister;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:47
 * @since 1.0
 */
public class CommandSyntaxError extends CommandError {

  public CommandSyntaxError(CommandContext context, CommandNode node) {
    this((register) -> Message.ERROR_SYNTAX.substitute(new LookupPopulator()
        .put(ArrayPath.of(), context)
        .put(ArrayPath.of(), node)
        .getLookup()));
  }

  public CommandSyntaxError(@Nullable Message key) {
    super(key);
  }

  public CommandSyntaxError(@Nullable Function<MessageRegister, String> messageFactory) {
    super(messageFactory);
  }

  public CommandSyntaxError(String message) {
    super(message);
  }

  public CommandSyntaxError(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandSyntaxError(Throwable cause) {
    super(cause);
  }

  public CommandSyntaxError(String message, Throwable cause, boolean enableSuppression,
                            boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
