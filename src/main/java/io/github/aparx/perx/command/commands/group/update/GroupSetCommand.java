package io.github.aparx.perx.command.commands.group.update;

import com.google.common.collect.ImmutableList;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.commands.group.AbstractGroupCommand;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 09:52
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupSetCommand extends CommandNode {

  public GroupSetCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("set")
        .require(PerxCommand.PERMISSION_GROUP_MANAGE)
        .build());
    for (GroupUpdateField field : GroupUpdateField.values())
      addChild(new GroupUpdateFieldCommand(field));
  }

  final class GroupUpdateFieldCommand extends AbstractGroupCommand {
    final GroupUpdateField field;

    public GroupUpdateFieldCommand(GroupUpdateField field) {
      super(GroupSetCommand.this, CommandNodeInfo.builder(field.getName())
          .usage("<Group> " + (!field.getSuggestions().isEmpty()
              ? String.format("(%s)", String.join(":", field.getSuggestions()))
              : "(Value)"))
          .require(PerxCommand.PERMISSION_GROUP_MANAGE)
          .description(String.format("Update the %s of a group", field.getName()))
          .build());
      this.field = field;
    }

    @Override
    protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
      GroupUpdateField.FieldExecutor fieldExecutor = field.getFieldExecutor();
      fieldExecutor.execute(group, args);
      CommandSender sender = context.sender();
      sender.sendMessage((switch (field) {
        case PREFIX -> MessageKey.GROUP_UPDATE_PREFIX;
        case SUFFIX -> MessageKey.GROUP_UPDATE_SUFFIX;
        case PRIORITY -> MessageKey.GROUP_UPDATE_PRIORITY;
        case DEFAULT -> MessageKey.GROUP_UPDATE_DEFAULT;
      }).substitute(new LookupPopulator()
          .put(ArrayPath.of("group"), group)
          .getLookup()));
      group.update().exceptionally((__) -> 0).thenAccept((x) -> {
        StringLookup lookup = new LookupPopulator()
            .put(ArrayPath.of("context"), context)
            .put(ArrayPath.of("group"), group, /*nil*/ "none")
            .getLookup();
        if (x == 0) sender.sendMessage(MessageKey.GROUP_UPDATE_FAIL.substitute(lookup));
        else sender.sendMessage(MessageKey.GROUP_UPDATE_SUCCESS.substitute(lookup));
      });
    }

    @Override
    public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
      if (args.length() == 2) {
        ImmutableList<String> suggestions = field.getSuggestions();
        if (!suggestions.isEmpty()) return suggestions;
      }
      return super.tabComplete(context, args);
    }
  }
}
