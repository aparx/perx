package io.github.aparx.perx.config.configs;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.config.*;
import io.github.aparx.perx.message.LocalizedMessage;
import io.github.aparx.perx.message.MessageRegister;
import io.github.aparx.perx.message.Message;
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
    @Nullable Message byReference = Message.getByReference(variableName);
    if (byReference != null)
      return byReference.get(register).getMessage();
    @Nullable Message byPath = Message.getByPath(ArrayPath.parse(variableName));
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
      @Nullable Message message;
      if ((message = Message.getByPath(ArrayPath.parse(key))) != null)
        register.set(message.getPath(), createMessage(config.get(key)));
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
        .set(Message.PREFIX, "&e[Perx]&r")
        .set(Message.SUCCESS_PREFIX, "&a[Perx]")
        .set(Message.ERROR_PREFIX, "&c[Perx]")
        .set(Message.JOIN, "&a&l+&r {player.displayName}&r joined the server!")
        .set(Message.QUIT, "&c&l-&r {player.displayName}&r has left the server!")
        .set(Message.GENERIC_LOADING, "{prefix}&7 Loading (...)")
        .set(Message.GENERIC_GROUP_NOT_FOUND, "{prefix.error} Could not find group {name}!")
        .set(Message.GENERIC_GROUP_NONE_EXISTING, "{prefix.error} No groups currently exist!")
        .set(Message.ERROR_SYNTAX, "{prefix.error} Syntax: /{fullUsage}")
        .set(Message.ERROR_PLAYER, "{prefix.error} You need to be a player for this action!")
        .set(Message.ERROR_PERMISSION, "{prefix.error} Missing permissions: {permissions}")
        .set(Message.ERROR_NUMBER_RANGE, "{prefix.error} Number must be between {min} and {max}!")
        .set(Message.ERROR_NAME_TOO_LONG, "{prefix.error} The name must be less than {max} characters!")
        .set(Message.ERROR_PREFIX_TOO_LONG, "{prefix.error} The prefix must be less than {max} characters!")
        .set(Message.ERROR_SUFFIX_TOO_LONG, "{prefix.error} The suffix must be less than {max} characters!")
        .set(Message.GROUP_CREATE_NAME, "{prefix.error} The name must not contain special characters!")
        .set(Message.GROUP_CREATE_DUPLICATE, "{prefix.error} This group already exists!")
        .set(Message.GROUP_CREATE_FAIL, "{prefix.error} Could not create group!")
        .set(Message.GROUP_CREATE_SUCCESS, "{prefix.success} Created group &r{group.name}&a!")
        .set(Message.GROUP_DELETE_FAIL, "{prefix.error} Could not delete {group.name}!")
        .set(Message.GROUP_DELETE_SUCCESS, "{prefix.success} Deleted group &r{group.name}&a!")
        .set(Message.GROUP_UPDATE_FAIL, "{prefix.error} Could not update group {group.name}!")
        .set(Message.GROUP_UPDATE_SUCCESS, "{prefix.success} Updated group &r{group.name}&a!")
        .set(Message.GROUP_UPDATE_PREFIX, "{prefix}&7 Updating prefix of &e{group.name}&7 to '{group.prefix}&7' (...)")
        .set(Message.GROUP_UPDATE_SUFFIX, "{prefix}&7 Updating suffix of &e{group.name}&7 to '{group.suffix}&7' (...)")
        .set(Message.GROUP_UPDATE_PRIORITY, "{prefix}&7 Updating priority of &e{group.name}&7 to &e{group.priority}&7 (...)")
        .set(Message.GROUP_UPDATE_DEFAULT, "{prefix}&7 Updating default mode of &e{group.name}&7 to &e{group.default}&7 (...)")
        .set(Message.GENERIC_GROUP_NOT_SUBSCRIBED, "{prefix.error} Not subscribed to group {group.name}!")
        .set(Message.GROUP_ADD_DUPLICATE, "{prefix.error} Player is already added in group {group.name}!")
        .set(Message.GROUP_ADD_TOO_SHORT, "{prefix.error} Duration must be more or equal to one second!")
        .set(Message.GROUP_ADD_FAIL, "{prefix.error} Could not add {target.name} to {group.name}!")
        .set(Message.GROUP_ADD_SUCCESS, "{prefix.success} Added player &r{target.name}&a to group &r{group.name}&a for &7{duration}&a!")
        .set(Message.GROUP_REMOVE_FAIL, "{prefix.error} Could not remove {target.name} from {group.name}!")
        .set(Message.GROUP_REMOVE_SUCCESS, "{prefix.success} Removed player &r{target.name}&a from group &r{group.name}&a!")
        .set(Message.GROUP_PURGE_FAIL, "{prefix.error} Could not purge {group.name}!")
        .set(Message.GROUP_PURGE_SUCCESS, "{prefix.success} Successfully purged group &r{group.name}&a!")
        .set(Message.GROUP_PERM_SET_FAIL, "{prefix.error} Could not set {perm.name} ({perm.value}) to {group.name}!")
        .set(Message.GROUP_PERM_SET_SUCCESS, "{prefix.success} Set permission &7{perm.name}&a in group &7{group.name}&a to &r{perm.value}&a!")
        .set(Message.GROUP_PERM_UNSET_FAIL, "{prefix.error} Could not unset {perm.name} in group {group.name}!")
        .set(Message.GROUP_PERM_UNSET_NOT_FOUND, "{prefix.error} Could not find {perm.name} in group {group.name}!")
        .set(Message.GROUP_PERM_UNSET_SUCCESS, "{prefix.success} Unset permission &7{perm.name}&a ({perm.value}) in group &7{group.name}&a!")
        .build(register);
  }

}
