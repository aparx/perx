package io.github.aparx.perx.command.commands.group.update;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.message.MessageKey;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 09:53
 * @since 1.0
 */
public enum GroupUpdateField {

  PREFIX((group, list) -> {
    int maxLength = GroupStyleKey.PREFIX.getMaxLength();
    String value = list.join();
    CommandAssertion.checkTrue(value.length() <= maxLength,
        (reg) -> MessageKey.ERROR_PREFIX_TOO_LONG.substitute(Map.of("max", maxLength)));
    group.setStyle(GroupStyleKey.PREFIX, (StringUtils.isNotEmpty(value) ? value + ' ' : null));
  }),

  SUFFIX((group, list) -> {
    int maxLength = GroupStyleKey.SUFFIX.getMaxLength();
    String value = list.join();
    CommandAssertion.checkTrue(value.length() <= maxLength,
        (reg) -> MessageKey.ERROR_SUFFIX_TOO_LONG.substitute(Map.of("max", maxLength)));
    group.setStyle(GroupStyleKey.SUFFIX, (StringUtils.isNotEmpty(value) ? ' ' + value : null));
  }),

  PRIORITY((group, list) -> {
    group.setPriority(!list.isEmpty() ? list.first().getInt() : PerxGroup.DEFAULT_PRIORITY);
  }),

  DEFAULT((group, list) -> {
    group.setDefault(!list.isEmpty() && list.first().getBoolean());
  }, ImmutableList.of("true", "false"));

  private final String name;
  private final FieldExecutor fieldExecutor;
  private final ImmutableList<String> suggestions;

  GroupUpdateField(FieldExecutor fieldExecutor) {
    this(fieldExecutor, ImmutableList.of());
  }

  GroupUpdateField(FieldExecutor fieldExecutor, ImmutableList<String> suggestions) {
    Preconditions.checkNotNull(fieldExecutor, "Executor must not be null");
    Preconditions.checkNotNull(suggestions, "Suggestions must not be null");
    this.name = name().toLowerCase(Locale.ENGLISH);
    this.fieldExecutor = fieldExecutor;
    this.suggestions = suggestions;
  }

  public String getName() {
    return name;
  }

  public FieldExecutor getFieldExecutor() {
    return fieldExecutor;
  }

  public ImmutableList<String> getSuggestions() {
    return suggestions;
  }

  @FunctionalInterface
  public interface FieldExecutor {
    void execute(PerxGroup group, CommandArgumentList list) throws CommandError;
  }

}
