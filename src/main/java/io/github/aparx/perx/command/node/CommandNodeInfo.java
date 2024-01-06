package io.github.aparx.perx.command.node;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 15:26
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public record CommandNodeInfo(
    String name,
    ImmutableList<String> permissions,
    @Nullable String description,
    @Nullable String usage
) {

  public CommandNodeInfo {
    Preconditions.checkNotNull(name, "Name must not be null");
    Validate.notBlank(name, "Name must not be blank");
  }

  public static CommandNodeInfo of(String name) {
    return new CommandNodeInfo(name, ImmutableList.of(), null, null);
  }

  public static CommandNodeInfoBuilder builder() {
    return CommandNodeInfoBuilder.builder();
  }

  public static CommandNodeInfoBuilder builder(String name) {
    return CommandNodeInfoBuilder.builder(name);
  }

  public boolean hasPermission() {
    return !permissions.isEmpty();
  }

  public boolean hasDescription() {
    return StringUtils.isNotEmpty(description);
  }

  public boolean hasUsage() {
    return StringUtils.isNotEmpty(usage);
  }
}
