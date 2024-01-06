package io.github.aparx.perx.command.node;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:21
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public final class CommandNodeInfoBuilder {

  private @Nullable String name;
  private @Nullable String description;
  private @Nullable String usage;

  private ImmutableList.Builder<String> permissions = ImmutableList.builder();

  private CommandNodeInfoBuilder() {}

  public static CommandNodeInfoBuilder builder() {
    return new CommandNodeInfoBuilder();
  }

  public static CommandNodeInfoBuilder builder(String name) {
    return new CommandNodeInfoBuilder().name(name);
  }

  public static CommandNodeInfoBuilder builder(CommandNodeInfoBuilder builder) {
    CommandNodeInfoBuilder copy = new CommandNodeInfoBuilder();
    copy.name = builder.name;
    copy.usage = builder.usage;
    copy.permissions = ImmutableList.builder();
    copy.permissions.addAll(builder.permissions.build());
    copy.description = builder.description;
    return copy;
  }

  @CanIgnoreReturnValue
  public CommandNodeInfoBuilder name(String name) {
    Preconditions.checkNotNull(name, "Name must not be null");
    Validate.notBlank(name, "Name must not be blank");
    this.name = name;
    return this;
  }

  @CanIgnoreReturnValue
  public CommandNodeInfoBuilder permission(String permission) {
    Preconditions.checkNotNull(permission, "Permission must not be null");
    this.permissions.add(permission);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandNodeInfoBuilder permission(Collection<String> permissions) {
    Validate.noNullElements(permissions, "Permission(s) must not be null");
    this.permissions = ImmutableList.builder();
    this.permissions.addAll(permissions);
    return this;
  }

  @CanIgnoreReturnValue
  public CommandNodeInfoBuilder description(@Nullable String description) {
    this.description = description;
    return this;
  }

  @CanIgnoreReturnValue
  public CommandNodeInfoBuilder usage(@Nullable String usage) {
    this.usage = usage;
    return this;
  }

  @CheckReturnValue
  public CommandNodeInfo build() {
    Preconditions.checkNotNull(name, "Name must be present (forgot the definition)");
    return new CommandNodeInfo(name, permissions.build(), description, usage);
  }

}
