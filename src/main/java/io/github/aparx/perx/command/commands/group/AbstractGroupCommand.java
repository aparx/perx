package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.controller.PerxGroupController;
import io.github.aparx.perx.message.MessageKey;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 08:04
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public abstract class AbstractGroupCommand extends CommandNode {

  public AbstractGroupCommand(CommandNode parent, CommandNodeInfo info) {
    super(parent, info);
  }

  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    tryCascadeExecute(context, args); // cascades execution down to matching child(ren)
  }

  @Override
  public final void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.isEmpty())
      throw createSyntaxError(context);
    @Nullable PerxGroup perxGroup = args.first().getGroup();
    CommandAssertion.checkTrue(perxGroup != null, (lang) ->
        MessageKey.GENERIC_GROUP_NOT_FOUND.substitute(lang, Map.of("name", args.first().value())));
    execute(context, args.skip(), perxGroup);
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 1)
      return super.tabComplete(context, args.skip());
    if (args.isEmpty()) return null;
    PerxGroupController groupController = Perx.getInstance().getGroupController();
    List<String> list = new ArrayList<>(Math.min(16, groupController.size()));
    String filterByName = args.getString(0);
    for (PerxGroup group : groupController)
      if (StringUtils.startsWithIgnoreCase(group.getName(), filterByName))
        list.add(group.getName());
    return list;
  }
}
