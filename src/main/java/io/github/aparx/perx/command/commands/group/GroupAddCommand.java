package io.github.aparx.perx.command.commands.group;

import com.google.common.base.Preconditions;
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
import io.github.aparx.perx.utils.duration.DurationParseException;
import io.github.aparx.perx.utils.duration.DurationParser;
import io.github.aparx.perx.utils.duration.DurationProcessor;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:41
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupAddCommand extends AbstractGroupCommand {

  private final DurationProcessor processor;

  public GroupAddCommand(CommandNode parent) {
    this(parent, DurationParser.DEFAULT_PARSER);
  }

  public GroupAddCommand(CommandNode parent, DurationProcessor processor) {
    super(parent, CommandNodeInfo.builder("add")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Add players to a group (Duration example: 4d7m23s)")
        .usage("<Group> <Player> (Duration)")
        .build());
    Preconditions.checkNotNull(processor, "Processor must not be null");
    this.processor = processor;
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
    CommandAssertion.checkFalse(user != null && user.hasGroup(group.getName()), (lang) -> {
      return Message.GROUP_ADD_DUPLICATE.substitute(lang, populator.getLookup());
    });
    @Nullable Date endDate = null;
    if (args.length() >= 2) {
      Duration parse = parseDuration(args.join(1));
      populator.put(ArrayPath.of("duration"), parse);
      CommandAssertion.checkTrue(parse.toSeconds() > 0, (lang) ->
          Message.GROUP_ADD_TOO_SHORT.substitute(populator.getLookup()));
      endDate = new Date(new Date().toInstant().plus(parse).toEpochMilli());
    } else populator.put(ArrayPath.of("duration"), "permanent");
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    context.respond(Message.GENERIC_LOADING);
    groupHandler.subscribe(target.getUniqueId(), group.getName(), endDate)
        .exceptionally((__) -> false)
        .thenAccept((res) -> {
          StringLookup lookup = populator.getLookup();
          if (res) context.respond(Message.GROUP_ADD_SUCCESS.substitute(lookup));
          else context.respond(Message.GROUP_ADD_FAIL.substitute(lookup));
        });
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 2) return super.tabComplete(context, args);
    PerxUserService userService = Perx.getInstance().getUserService();
    @Nullable PerxGroup group = args.first().getGroup();
    return getPlayerCompletionStream(context, args.getString(1))
        .filter((player) -> {
          if (group == null) return true;
          @Nullable PerxUser user = userService.get(player.getUniqueId());
          return (user != null && !user.hasGroup(group.getName()));
        })
        .map(Player::getName)
        .collect(Collectors.toList());
  }

  protected Duration parseDuration(String line) throws CommandError {
    try {
      return processor.parse(line);
    } catch (DurationParseException e) {
      throw new CommandError(e.getMessage(), e);
    }
  }
}
