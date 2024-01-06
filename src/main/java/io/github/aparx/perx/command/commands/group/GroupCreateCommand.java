package io.github.aparx.perx.command.commands.group;

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
import io.github.aparx.perx.group.controller.PerxGroupController;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.MessageKey;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 08:04
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupCreateCommand extends CommandNode {

  public static final int MAXIMUM_NAME_LENGTH = 32;
  public static final Pattern NAME_PATTERN =
      Pattern.compile("[A-z0-9_.-]{1," + MAXIMUM_NAME_LENGTH + "}");

  public GroupCreateCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("create")
        .permission(PerxCommand.PERMISSION_MANAGE)
        .usage("<Name> (Default <true:false>)")
        .description("Create a new empty permissions group")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.isEmpty())
      throw createSyntaxError(context);
    String name = args.getString(0);
    // (1) assert that name is not too long (which is also done through the pattern)
    CommandAssertion.checkTrue(name.length() < MAXIMUM_NAME_LENGTH,
        (lang) -> MessageKey.ERROR_NAME_TOO_LONG.substitute(Map.of("max", MAXIMUM_NAME_LENGTH)));
    // (2) assert that name is matching the given pattern
    CommandAssertion.checkTrue(NAME_PATTERN.matcher(name).matches(),
        (lang) -> MessageKey.GROUP_CREATE_NAME.substitute(lang, Map.of(
            "min", 1, "max", MAXIMUM_NAME_LENGTH, "pattern", NAME_PATTERN.pattern()
        )));
    boolean isDefault = args.length() > 1 && Boolean.parseBoolean(args.getString(1));
    // (3) assert that this group is not a duplicate
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    CommandAssertion.checkFalse(groupController.contains(name),
        (lang) -> MessageKey.GROUP_CREATE_DUPLICATE.substitute(lang, Map.of("name", name)));
    PerxGroup perxGroup = PerxGroup.of(name);
    perxGroup.setDefault(isDefault);
    // (4) actually create and push the group (if possible)
    CommandSender sender = context.sender();
    sender.sendMessage(MessageKey.GENERIC_LOADING.substitute());
    PerxGroupHandler groupHandler = Perx.getInstance().getGroupHandler();
    groupHandler.create(perxGroup).exceptionally((__) -> false).thenAccept((res) -> {
      StringLookup lp = new LookupPopulator().put(ArrayPath.of("group"), perxGroup).getLookup();
      if (res) sender.sendMessage(MessageKey.GROUP_CREATE_SUCCESS.substitute(lp));
      else sender.sendMessage(MessageKey.GROUP_CREATE_FAIL.substitute(lp));
    });
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() == 2)
      return List.of("true", "false");
    return super.tabComplete(context, args);
  }
}
