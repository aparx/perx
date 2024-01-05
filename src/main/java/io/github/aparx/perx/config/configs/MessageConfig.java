package io.github.aparx.perx.config.configs;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.config.*;
import io.github.aparx.perx.message.LocalizedMessage;
import io.github.aparx.perx.message.MessageRegister;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 06:41
 * @since 1.0
 */
@ConfigHandleId("messages")
public class MessageConfig extends ConfigHandle {

  private final MessageRegister register;

  private final StringLookup defaultLookup = (variableName) -> {
    MessageRegister register = getRegister();
    @Nullable MessageKey byReference = MessageKey.getByReference(variableName);
    if (byReference != null)
      return byReference.get(register).getMessage();
    @Nullable MessageKey byPath = MessageKey.getByPath(ArrayPath.parse(variableName));
    if (byPath != null)
      return byPath.get(register).getMessage();
    return null;
  };

  public MessageConfig(ConfigManager configManager) {
    this(configManager, Perx.getInstance().getMessages());
  }

  public MessageConfig(ConfigManager configManager, MessageRegister register) {
    super(configManager);
    this.register = register;
  }

  public MessageRegister getRegister() {
    return register;
  }

  @Override
  public void init(@NonNull Config config) {
    putDefaultMessages();
    config.load();
    // 1. fill register with already defined messages
    for (String key : config.getConfig().getKeys(true)) {
      @Nullable MessageKey messageKey;
      if ((messageKey = MessageKey.getByPath(ArrayPath.parse(key))) != null)
        register.set(messageKey.getPath(), createMessage(config.get(key)));
    }
    // 2. fill config with missing messages
    register.forEach((path, message) -> {
      config.setIfAbsent(path, message.getRawMessage());
    });
    config.save();
  }

  private LocalizedMessage createMessage(Object message) {
    return LocalizedMessage.of(Objects.toString(message, null), defaultLookup);
  }

  private void putDefaultMessages() {
    DefaultMessagesBuilder.builder(this::createMessage)
        .set(MessageKey.PREFIX, "&e[Perx]&r")
        .set(MessageKey.SUCCESS_PREFIX, "&a[Perx]")
        .set(MessageKey.ERROR_PREFIX, "&c[Perx]")
        .set(MessageKey.JOIN, "&a&l+&r {player.displayName}&r joined the server!")
        .set(MessageKey.QUIT, "&a&l+&r {player.displayName}&r has left the server!")
        .build(register);
  }

}
