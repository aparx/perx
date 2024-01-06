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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
      String[] lines = message.toLines();
      config.setIfAbsent(path, lines.length == 1 ? lines[0] : lines);
    });
    config.save();
  }

  private LocalizedMessage createMessage(Object message) {
    if (message instanceof Collection<?>)
      return LocalizedMessage.of((Collection<?>) message, defaultLookup);
    if (message != null && message.getClass().isArray())
      return createMessage(Arrays.asList((Object[]) message));
    return LocalizedMessage.of(Objects.toString(message, "null"), defaultLookup);
  }

  // @formatter:off
  private void putDefaultMessages() {
    DefaultMessagesBuilder.builder(this::createMessage)
        .set(MessageKey.PREFIX, "&e[Perx]&r")
        .set(MessageKey.SUCCESS_PREFIX, "&a[Perx]")
        .set(MessageKey.ERROR_PREFIX, "&c[Perx]")
        .set(MessageKey.JOIN, "&a&l+&r {player.displayName}&r joined the server!")
        .set(MessageKey.QUIT, "&c&l-&r {player.displayName}&r has left the server!")
        .set(MessageKey.GENERIC_LOADING, "{prefix}&7 Loading (...)")
        .set(MessageKey.GENERIC_GROUP_NOT_FOUND, "{prefix.error} Could not find group {name}!")
        .set(MessageKey.GENERIC_GROUP_NONE_EXISTING, "{prefix.error} No groups currently exist!")
        .set(MessageKey.ERROR_SYNTAX, "{prefix.error} Syntax: /{fullUsage}")
        .set(MessageKey.ERROR_PLAYER, "{prefix.error} You need to be a player for this action!")
        .set(MessageKey.ERROR_PERMISSION, "{prefix.error} Missing permissions: {permissions}")
        .set(MessageKey.ERROR_NUMBER_RANGE, "{prefix.error} Number must be between {min} and {max}!")
        .set(MessageKey.ERROR_NAME_TOO_LONG, "{prefix.error} The name must be less than {max} characters!")
        .set(MessageKey.ERROR_PREFIX_TOO_LONG, "{prefix.error} The prefix must be less than {max} characters!")
        .set(MessageKey.ERROR_SUFFIX_TOO_LONG, "{prefix.error} The suffix must be less than {max} characters!")
        .set(MessageKey.GROUP_CREATE_NAME, "{prefix.error} The name must not contain special characters!")
        .set(MessageKey.GROUP_CREATE_DUPLICATE, "{prefix.error} This group already exists!")
        .set(MessageKey.GROUP_CREATE_FAIL, "{prefix.error} Could not create group!")
        .set(MessageKey.GROUP_CREATE_SUCCESS, "{prefix.success} Created group &r{group.name}&a!")
        .set(MessageKey.GROUP_DELETE_FAIL, "{prefix.error} Could not delete {group.name}!")
        .set(MessageKey.GROUP_DELETE_SUCCESS, "{prefix.success} Deleted group &r{group.name}&a!")
        .set(MessageKey.GROUP_UPDATE_FAIL, "{prefix.error} Could not update group {group.name}!")
        .set(MessageKey.GROUP_UPDATE_SUCCESS, "{prefix.success} Updated group &r{group.name}&a!")
        .set(MessageKey.GROUP_UPDATE_PREFIX, "{prefix}&7 Updating prefix of &e{group.name}&7 to '{group.prefix}&7' (...)")
        .set(MessageKey.GROUP_UPDATE_SUFFIX, "{prefix}&7 Updating suffix of &e{group.name}&7 to '{group.suffix}&7' (...)")
        .set(MessageKey.GROUP_UPDATE_PRIORITY, "{prefix}&7 Updating priority of &e{group.name}&7 to &e{group.priority}&7 (...)")
        .set(MessageKey.GROUP_UPDATE_DEFAULT, "{prefix}&7 Updating default mode of &e{group.name}&7 to &e{group.default}&7 (...)")
        .set(MessageKey.GROUP_INFO, List.of(
            "{prefix} Information about &e{group.name}&7:",
            "{prefix} &8• &7Prefix:&r {group.prefix}&r",
            "{prefix} &8• &7Suffix:&r {group.suffix}&r",
            "{prefix} &8• &7Default:&r {group.default.color}{group.default}&r",
            "{prefix} &8• &7Priority:&r {group.priority}&r"))
        .set(MessageKey.GENERIC_GROUP_NOT_SUBSCRIBED, "{prefix.error} Not subscribed to group {group.name}!")
        .set(MessageKey.GROUP_ADD_DUPLICATE, "{prefix.error} Player is already added in group {group.name}!")
        .set(MessageKey.GROUP_ADD_TOO_SHORT, "{prefix.error} Duration must be more or equal to one second!")
        .set(MessageKey.GROUP_ADD_FAIL, "{prefix.error} Could not add {target.name} to {group.name}!")
        .set(MessageKey.GROUP_ADD_SUCCESS, "{prefix.success} Added player &r{target.name}&a to group &r{group.name}&a for &7{duration}&a!")
        .set(MessageKey.GROUP_REMOVE_FAIL, "{prefix.error} Could not remove {target.name} from {group.name}!")
        .set(MessageKey.GROUP_REMOVE_SUCCESS, "{prefix.success} Removed player &r{target.name}&a from group &r{group.name}&a!")
        .set(MessageKey.GROUP_PURGE_FAIL, "{prefix.error} Could not purge {group.name}!")
        .set(MessageKey.GROUP_PURGE_SUCCESS, "{prefix.success} Successfully purged group &r{group.name}&a!")
        .build(register);
  }

}
