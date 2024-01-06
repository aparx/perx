package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupHandler;
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
 * @version 2024-01-06 14:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupPurgeCommand extends AbstractGroupCommand {

  public GroupPurgeCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("purge")
        .permission(PerxCommand.PERMISSION_MANAGE)
        .description("Removes all players from a group")
        .usage("<Group>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (!args.isEmpty()) throw createSyntaxError(context);
    CommandSender sender = context.sender();
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    sender.sendMessage(MessageKey.GENERIC_LOADING.substitute());
    groupHandler.unsubscribe(group).exceptionally((__) -> false).thenAccept((res) -> {
      StringLookup lp = new LookupPopulator().put(ArrayPath.of("group"), group).getLookup();
      if (res) sender.sendMessage(MessageKey.GROUP_PURGE_SUCCESS.substitute(lp));
      else sender.sendMessage(MessageKey.GROUP_PURGE_FAIL.substitute(lp));
    });
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 2) return super.tabComplete(context, args);
    // TODO only complete players that are in that group (through cache)
    return tabCompletePlayers(context);
  }
}
