package io.github.aparx.perx.command.errors;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.message.MessageRegister;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:48
 * @since 1.0
 */
public class CommandError extends Exception {

  private final @Nullable Function<MessageRegister, @Nullable String> messageFactory;

  public CommandError(@Nullable MessageKey key) {
    this((register) -> (key != null ? key.get(register).getMessage() : null));
  }

  public CommandError(@Nullable Function<MessageRegister, String> messageFactory) {
    this.messageFactory = messageFactory;
  }

  public CommandError(String message) {
    super(message);
    this.messageFactory = (__) -> message;
  }

  public CommandError(String message, Throwable cause) {
    super(message, cause);
    this.messageFactory = (__) -> message;
  }

  public CommandError(Throwable cause) {
    super(cause);
    this.messageFactory = (__) -> cause.getMessage();
  }

  public CommandError(String message, Throwable cause, boolean enableSuppression,
                      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.messageFactory = (__) -> message;
  }

  @Override
  public @Nullable String getLocalizedMessage() {
    return getLocalizedMessage(Perx.getInstance().getMessages());
  }

  public @Nullable String getLocalizedMessage(MessageRegister register) {
    return (messageFactory != null ? messageFactory.apply(register) : getMessage());
  }

}
