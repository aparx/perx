package io.github.aparx.perx.command.commands;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.duration.DurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 13:15
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class InfoCommand extends CommandNode {

  public InfoCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("info")
        .permission(PerxPermissions.PERMISSION_INFO_SELF)
        .permission(PerxPermissions.PERMISSION_INFO_OTHER)
        .usage("<Player>")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.length() > 1) throw createSyntaxError(context);
    if (args.isEmpty()) CommandAssertion.checkIsPlayer(context);
    OfflinePlayer targetPlayer = (args.isEmpty()
        ? context.getPlayer()
        : args.first().getOfflinePlayer());
    CommandSender sender = context.sender();
    if (context.isPlayer()
        && targetPlayer.equals(context.getPlayer())
        && !sender.hasPermission(PerxPermissions.PERMISSION_INFO_OTHER))
      throw createPermissionError(PerxPermissions.PERMISSION_INFO_OTHER);

    PerxUserController userController = Perx.getInstance().getUserController();
    context.respond(StringUtils.SPACE);
    if (!userController.contains(targetPlayer))
      context.respond(Message.GENERIC_LOADING);
    userController.fetchOrGet(targetPlayer, UserCacheStrategy.TEMPORARY).thenAccept((user) -> {
      OfflinePlayer offline = user.getOffline();
      Collection<PerxUserGroup> subscribed = new ArrayList<>(user.getSubscribed());
      if (!offline.isOnline())
        // insert all default groups, since the player is offline and will not have
        // the default groups added
        subscribed.addAll(Perx.getInstance()
            .getGroupController()
            .getDefaults().stream()
            .map((defaultGroup) -> PerxUserGroup.of(offline.getUniqueId(), defaultGroup))
            .toList());
      if (subscribed.isEmpty()) {
        context.respond(Message.PREFIX + " This user has no groups.");
        return;
      }
      context.respond(String.format("%s Groups of %s:", Message.PREFIX, targetPlayer.getName()));
      subscribed.stream()
          .sorted(PerxUserGroup.USER_GROUP_COMPARATOR.reversed())
          .forEach((group) -> context.respond(createUserGroupDisplay(group)));
    });
  }

  private String createUserGroupDisplay(PerxUserGroup userGroup) {
    PerxGroup group = userGroup.getGroup();
    StringBuilder builder = new StringBuilder()
        .append(Message.PREFIX)
        .append(' ')
        .append(ChatColor.GRAY)
        .append("• ")
        .append(group.getName())
        .append(' ');
    @Nullable Date endingDate = userGroup.getEndingDate();
    if (endingDate != null) {
      builder.append(ChatColor.YELLOW).append('[')
          .append(DurationUtils.createTimeLeft(new Date(), endingDate))
          .append(']');
    } else
      builder.append(ChatColor.DARK_GRAY).append("permanent");
    if (!userGroup.isModelInDatabase())
      builder.append(' ').append(ChatColor.DARK_GRAY).append("(default)");
    return builder.toString();
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    return (args.length() == 1
        ? tabCompletePlayers(context, args.getString(0))
        : super.tabComplete(context, args));
  }
}

