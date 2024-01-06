package io.github.aparx.perx.config;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.message.LocalizedMessage;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.message.MessageRegister;
import io.github.aparx.perx.utils.ArrayPath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Collection;
import java.util.EnumMap;
import java.util.function.Function;

/**
 * Helpful builder for assigning messages to {@code MessageKey}s, that throws a runtime exception
 * when a message is missing.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 08:11
 * @apiNote This is really helpful for development, for when contributors and maintainers of this
 * project extend the messages, but forget to put a default message.
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class DefaultMessagesBuilder {

  private final EnumMap<MessageKey, LocalizedMessage> messages;
  private final Function<String, LocalizedMessage> messageFactory;

  private DefaultMessagesBuilder(
      EnumMap<MessageKey, LocalizedMessage> messages,
      Function<String, LocalizedMessage> messageFactory) {
    Preconditions.checkNotNull(messages, "Messages must not be null");
    Preconditions.checkNotNull(messageFactory, "Message factory must not be null");
    this.messages = messages;
    this.messageFactory = messageFactory;
  }

  public static DefaultMessagesBuilder builder(Function<String, LocalizedMessage> messageFactory) {
    return new DefaultMessagesBuilder(new EnumMap<>(MessageKey.class), messageFactory);
  }

  public Function<String, LocalizedMessage> getFactory() {
    return messageFactory;
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(ArrayPath path, LocalizedMessage message) {
    Preconditions.checkNotNull(path, "Path must not be null");
    Preconditions.checkNotNull(message, "Message must not be null");
    @Nullable MessageKey byPath = MessageKey.getByPath(path);
    Preconditions.checkNotNull(byPath, "Path not a valid default message key");
    messages.put(byPath, message);
    return this;
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(MessageKey key, LocalizedMessage message) {
    Preconditions.checkNotNull(key, "Key must not be null");
    Preconditions.checkNotNull(message, "Message must not be null");
    messages.put(key, message);
    return this;
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(ArrayPath path, String message) {
    return set(path, messageFactory.apply(message));
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(MessageKey key, String message) {
    return set(key, messageFactory.apply(message));
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(ArrayPath path, Collection<String> message) {
    return set(path, messageFactory.apply(LocalizedMessage.join(message)));
  }

  @CanIgnoreReturnValue
  public DefaultMessagesBuilder set(MessageKey key, Collection<String> message) {
    return set(key, messageFactory.apply(LocalizedMessage.join(message)));
  }

  public void build(MessageRegister register) {
    for (MessageKey key : MessageKey.values()) {
      @Nullable LocalizedMessage message = messages.get(key);
      Preconditions.checkNotNull(message, "Missing message for: " + key.getPath());
      key.set(register, message);
    }
  }

}
