package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 07:23
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public enum MessageKey {

  /* +----- prefix -----+ */
  PREFIX("prefix.default", "prefix"),
  SUCCESS_PREFIX("prefix.success"),
  ERROR_PREFIX("prefix.error"),
  /* +----- broadcast -----+ */
  JOIN("broadcast.join"),
  QUIT("broadcast.quit"),
  /* +----- generic -----+ */
  GENERIC_LOADING("generic.loading"),
  GENERIC_GROUP_NOT_FOUND("generic.group not found"),
  GENERIC_GROUP_NOT_SUBSCRIBED("generic.group not subscribed"),
  GENERIC_GROUP_NONE_EXISTING("commands.group.list.error none existing"),
  /* +----- errors -----+ */
  ERROR_PLAYER("errors.not a player"),
  ERROR_SYNTAX("errors.syntax", "syntax"),
  ERROR_PERMISSION("errors.permissions"),
  ERROR_NUMBER_RANGE("errors.number range"),
  ERROR_NAME_TOO_LONG("errors.name too long"),
  ERROR_PREFIX_TOO_LONG("errors.prefix too long"),
  ERROR_SUFFIX_TOO_LONG("errors.suffix too long"),
  /* +----- command: group create -----+ */
  GROUP_CREATE_NAME("commands.group.create.error name"),
  GROUP_CREATE_DUPLICATE("commands.group.create.error duplicate"),
  GROUP_CREATE_FAIL("commands.group.create.error fail"),
  GROUP_CREATE_SUCCESS("commands.group.create.success"),
  /* +----- command: group delete -----+ */
  GROUP_DELETE_FAIL("commands.group.delete.error fail"),
  GROUP_DELETE_SUCCESS("commands.group.delete.success"),
  /* +----- command: group set <...> -----+ */
  GROUP_UPDATE_PREFIX("commands.group.update.prefix"),
  GROUP_UPDATE_SUFFIX("commands.group.update.suffix"),
  GROUP_UPDATE_PRIORITY("commands.group.update.priority"),
  GROUP_UPDATE_DEFAULT("commands.group.update.default"),
  GROUP_UPDATE_FAIL("commands.group.update.fail"),
  GROUP_UPDATE_SUCCESS("commands.group.update.success"),
  /* +----- command: group info <...> -----+ */
  GROUP_INFO("commands.group.info"),
  /* +----- command: group add <...> -----+ */
  GROUP_ADD_DUPLICATE("commands.group.add.error duplicate"),
  GROUP_ADD_TOO_SHORT("commands.group.add.error duration too short"),
  GROUP_ADD_FAIL("commands.group.add.error fail"),
  GROUP_ADD_SUCCESS("commands.group.add.success"),
  /* +----- command: group remove <...> -----+ */
  GROUP_REMOVE_FAIL("commands.group.remove.error fail"),
  GROUP_REMOVE_SUCCESS("commands.group.remove.success"),
  /* +----- command: group purge <...> -----+ */
  GROUP_PURGE_FAIL("commands.group.purge.error fail"),
  GROUP_PURGE_SUCCESS("commands.group.purge.success"),
  ;

  private static final ImmutableMap<ArrayPath, MessageKey> byPath;
  private static final ImmutableMap<String, MessageKey> byReference;

  static {
    MessageKey[] values = values();
    ImmutableMap.Builder<ArrayPath, MessageKey> byPathBuilder =
        ImmutableMap.builderWithExpectedSize(values.length);
    ImmutableMap.Builder<String, MessageKey> byRefBuilder =
        ImmutableMap.builderWithExpectedSize(values.length);
    for (MessageKey key : values) {
      byPathBuilder.put(key.getPath(), key);
      byRefBuilder.put(key.getReference(), key);
    }
    byPath = byPathBuilder.build();
    byReference = byRefBuilder.build();
  }

  private final ArrayPath path;
  private final String reference;

  MessageKey(String path) {
    this(path, path);
  }

  MessageKey(String path, String reference) {
    this(ArrayPath.parse(path), reference);
  }

  MessageKey(ArrayPath path, String reference) {
    Preconditions.checkNotNull(path);
    this.path = path;
    this.reference = reference;
  }

  public static @Nullable MessageKey getByPath(ArrayPath path) {
    return byPath.get(path);
  }

  public static @Nullable MessageKey getByReference(String reference) {
    return byReference.get(reference);
  }

  public LocalizedMessage get(MessageRegister register) {
    return register.get(getPath());
  }

  public LocalizedMessage get() {
    return get(Perx.getInstance().getMessages());
  }

  public String substitute(MessageRegister register) {
    return get(register).substitute();
  }

  public String substitute() {
    return substitute(Perx.getInstance().getMessages());
  }

  public String substitute(MessageRegister register, StringLookup lookup) {
    return get(register).substitute(lookup);
  }

  public String substitute(StringLookup lookup) {
    return substitute(Perx.getInstance().getMessages(), lookup);
  }

  public String substitute(MessageRegister register, Map<String, ?> lookup) {
    return get(register).substitute(lookup);
  }

  public String substitute(Map<String, ?> lookup) {
    return substitute(Perx.getInstance().getMessages(), lookup);
  }

  @CanIgnoreReturnValue
  public @Nullable LocalizedMessage set(MessageRegister register, LocalizedMessage message) {
    return register.set(getPath(), message);
  }

  public ArrayPath getPath() {
    return path;
  }

  public String getReference() {
    return reference;
  }
}
