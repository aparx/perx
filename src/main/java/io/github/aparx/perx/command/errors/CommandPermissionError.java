package io.github.aparx.perx.command.errors;

import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.message.MessageRepository;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:47
 * @since 1.0
 */
public class CommandPermissionError extends CommandError {

  public CommandPermissionError(String permission) {
    this((register) -> Message.ERROR_PERMISSION.substitute(new LookupPopulator()
        .put(ArrayPath.of("permissions"), permission)
        .getLookup()));
  }

  public CommandPermissionError(CommandNode node) {
    this((register) -> Message.ERROR_PERMISSION.substitute(new LookupPopulator()
        .put(ArrayPath.of(), node)
        .getLookup()));
  }

  public CommandPermissionError(@Nullable Message key) {
    super(key);
  }

  public CommandPermissionError(@Nullable Function<MessageRepository, String> messageFactory) {
    super(messageFactory);
  }

  public CommandPermissionError(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandPermissionError(Throwable cause) {
    super(cause);
  }

  public CommandPermissionError(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
