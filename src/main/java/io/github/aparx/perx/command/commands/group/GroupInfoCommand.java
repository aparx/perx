package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 12:11
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupInfoCommand extends AbstractGroupCommand {

  public GroupInfoCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("info")
        .permission(PerxCommand.PERMISSION_GROUP_MANAGE)
        .description("Shows information about a group")
        .usage("<Group>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    CommandSender sender = context.sender();
    sender.sendMessage(StringUtils.SPACE);
    sender.sendMessage(MessageKey.GROUP_INFO.substitute(new LookupPopulator()
        .put(ArrayPath.of(), context)
        .put(ArrayPath.of("group"), group, "-")
        .getLookup()));
  }
}
