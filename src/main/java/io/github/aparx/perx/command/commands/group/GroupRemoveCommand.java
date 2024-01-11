package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupHandler;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.PerxUserService;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupRemoveCommand extends AbstractGroupCommand {

  public GroupRemoveCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("remove")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Remove players to a group")
        .usage("<Group> <Player>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (args.isEmpty()) throw createSyntaxError(context);
    OfflinePlayer target = args.first().getOfflinePlayer();
    PerxUserService userService = Perx.getInstance().getUserService();
    @Nullable PerxUser user = userService.get(target);
    LookupPopulator populator = new LookupPopulator()
        .put(ArrayPath.of("group"), group)
        .put(ArrayPath.of("target"), target);
    CommandAssertion.checkTrue(user == null || user.hasGroup(group.getName()), (lang) -> {
      return Message.GENERIC_GROUP_NOT_SUBSCRIBED.substitute(lang, populator.getLookup());
    });
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    context.respond(Message.GENERIC_LOADING);
    ((CompletableFuture<@Nullable Boolean>)
        groupHandler.unsubscribe(target.getUniqueId(), group.getName()))
        .exceptionally((__) -> null)
        .thenAccept((@Nullable Boolean res) -> {
          StringLookup lookup = populator.getLookup();
          if (res == null) context.respond(Message.GROUP_REMOVE_FAIL.substitute(lookup));
          else context.respond(Message.GROUP_REMOVE_SUCCESS.substitute(lookup));
        });
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 2) return super.tabComplete(context, args);
    PerxUserService userService = Perx.getInstance().getUserService();
    @Nullable PerxGroup group = args.first().getGroup();
    return getCompletingPlayerStream(context, args.getString(1))
        .filter((player) -> {
          if (group == null) return true;
          @Nullable PerxUser user = userService.get(player.getUniqueId());
          return user == null || user.hasGroup(group.getName());
        })
        .map(Player::getName)
        .collect(Collectors.toList());
  }
}
