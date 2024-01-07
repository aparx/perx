package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.commands.group.permission.GroupPermissionSetCommand;
import io.github.aparx.perx.command.commands.group.permission.GroupPermissionUnsetCommand;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 03:34
 * @since 1.0
 */
public class GroupPermissionCommand extends CommandNode {

  public GroupPermissionCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("perm")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .build());
    addChild(GroupPermissionSetCommand::new);
    addChild(GroupPermissionUnsetCommand::new);
  }

}
