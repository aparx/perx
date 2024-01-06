package io.github.aparx.perx.command.commands.group;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.CommandAssertion;
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
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.ArrayPath;
import io.github.aparx.perx.utils.duration.DurationParser;
import io.github.aparx.perx.utils.duration.DurationProcessor;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupRemoveCommand extends AbstractGroupCommand {

  public GroupRemoveCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("remove")
        .permission(PerxCommand.PERMISSION_GROUP_MANAGE)
        .description("Remove players to a group")
        .usage("<Group> <Player>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (args.isEmpty()) throw createSyntaxError(context);
    OfflinePlayer target = args.first().getOfflinePlayer();
    PerxUserController userController = Perx.getInstance().getUserController();
    @Nullable PerxUser user = userController.get(target);
    LookupPopulator populator = new LookupPopulator()
        .put(ArrayPath.of("group"), group)
        .put(ArrayPath.of("target"), target);
    CommandAssertion.checkTrue(user == null || user.hasGroup(group.getName()), (lang) -> {
      return MessageKey.GENERIC_GROUP_NOT_SUBSCRIBED.substitute(lang, populator.getLookup());
    });
    CommandSender sender = context.sender();
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    sender.sendMessage(MessageKey.GENERIC_LOADING.substitute());
    groupHandler.unsubscribe(target.getUniqueId(), group.getName())
        .exceptionally((__) -> false)
        .thenAccept((res) -> {
          StringLookup lookup = populator.getLookup();
          if (res) sender.sendMessage(MessageKey.GROUP_REMOVE_SUCCESS.substitute(lookup));
          else sender.sendMessage(MessageKey.GROUP_REMOVE_FAIL.substitute(lookup));
        });
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 2) return super.tabComplete(context, args);
    // TODO only complete players that are in that group (through cache)
    return tabCompletePlayers(context);
  }
}
