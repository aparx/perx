package io.github.aparx.perx.command.commands;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.UserCacheStrategy;
import io.github.aparx.perx.user.controller.PerxUserController;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
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
        .permission(PerxCommand.PERMISSION_INFO_SELF)
        .permission(PerxCommand.PERMISSION_INFO_OTHER)
        .usage("<Player>")
        .build());
  }

  private static String createTimeLeft(Duration duration) {
    long days = duration.toDays();
    long hours = (duration.toHours() % 24);
    long minutes = (duration.toMinutes() % 60);
    long seconds = (duration.toSeconds() % 60);
    List<String> units = new ArrayList<>();
    if (days > 0) units.add(days + "d");
    if (hours > 0) units.add(hours + "h");
    if (minutes > 0) units.add(minutes + "m");
    if (seconds > 0) units.add(seconds + "s");
    return String.join(StringUtils.SPACE, units);
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
        && !sender.hasPermission(PerxCommand.PERMISSION_INFO_OTHER))
      throw createPermissionError(PerxCommand.PERMISSION_INFO_OTHER);

    PerxUserController userController = Perx.getInstance().getUserController();
    sender.sendMessage(StringUtils.SPACE);
    if (!userController.contains(targetPlayer))
      sender.sendMessage(MessageKey.GENERIC_LOADING.substitute());
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
        sender.sendMessage(MessageKey.PREFIX.substitute() + " This user has no groups.");
        return;
      }
      sender.sendMessage(String.format("%s Groups of %s:",
          MessageKey.PREFIX.substitute(), targetPlayer.getName()));
      subscribed.stream()
          .sorted(PerxUserGroup.USER_GROUP_COMPARATOR.reversed())
          .forEach((group) -> sender.sendMessage(createUserGroupDisplay(group)));
    });
  }

  private String createUserGroupDisplay(PerxUserGroup userGroup) {
    PerxGroup group = userGroup.getGroup();
    StringBuilder builder = new StringBuilder()
        .append(MessageKey.PREFIX.substitute())
        .append(' ')
        .append(ChatColor.GRAY)
        .append("• ")
        .append(group.getName())
        .append(' ');
    @Nullable Date endingDate = userGroup.getEndingDate();
    if (endingDate != null) {
      builder.append(ChatColor.YELLOW).append('[').append(createTimeLeft(
          Duration.ofMillis(Math.max(endingDate.getTime() - System.currentTimeMillis(), 0))
      )).append(']');
    } else
      builder.append(ChatColor.DARK_GRAY).append("permanent");
    if (!userGroup.isModelInDatabase())
      builder.append(' ').append(ChatColor.DARK_GRAY).append("(default)");
    return builder.toString();
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    return (args.length() != 1 ? super.tabComplete(context, args) : tabCompletePlayers(context));
  }
}

