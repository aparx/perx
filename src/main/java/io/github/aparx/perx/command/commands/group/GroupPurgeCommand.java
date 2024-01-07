package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupPurgeCommand extends AbstractGroupCommand {

  public GroupPurgeCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("purge")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Removes all players from a group")
        .usage("<Group>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (!args.isEmpty()) throw createSyntaxError(context);
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    context.respond(Message.GENERIC_LOADING);
    groupHandler.unsubscribe(group).exceptionally((__) -> false).thenAccept((res) -> {
      StringLookup lp = new LookupPopulator().put(ArrayPath.of("group"), group).getLookup();
      if (res) context.respond(Message.GROUP_PURGE_SUCCESS.substitute(lp));
      else context.respond(Message.GROUP_PURGE_FAIL.substitute(lp));
    });
  }

}
