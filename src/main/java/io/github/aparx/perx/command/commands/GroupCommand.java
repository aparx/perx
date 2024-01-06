package io.github.aparx.perx.command.commands;

import io.github.aparx.perx.command.commands.group.GroupCreateCommand;
import io.github.aparx.perx.command.commands.group.GroupDeleteCommand;
import io.github.aparx.perx.command.commands.group.GroupInfoCommand;
import io.github.aparx.perx.command.commands.group.update.GroupSetCommand;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.command.node.CommandNodeInfoBuilder;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 08:03
 * @since 1.0
 */
public class GroupCommand extends CommandNode {

  public GroupCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.of("group"));
    addChild(GroupInfoCommand::new);
    addChild(GroupCreateCommand::new);
    addChild(GroupDeleteCommand::new);
    addChild(GroupSetCommand::new);
  }

}
