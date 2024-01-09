package io.github.aparx.perx.command.commands.group.permission;

import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.commands.group.AbstractGroupCommand;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.permission.PerxPermission;
import io.github.aparx.perx.permission.PerxPermissionRepository;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.lookup.StringLookup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 03:35
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupPermissionUnsetCommand extends AbstractGroupCommand {

  public GroupPermissionUnsetCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("unset")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Unset (reset) the permission of a group")
        .usage("<Group> <Permission>")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (args.length() != 1) throw createSyntaxError(context);
    String permission = args.getString(0);
    ArrayPath arrayPath = ArrayPath.parse(permission);
    PerxPermissionRepository permissionRegister = group.getRepository();
    @Nullable PerxPermission perm = permissionRegister.get(arrayPath);
    StringLookup lookup = new LookupPopulator()
        .put(ArrayPath.of("group"), group)
        .put(ArrayPath.of("perm", "name"), arrayPath.join())
        .put(ArrayPath.of("perm"), perm)
        .getLookup();
    CommandAssertion.checkTrue(perm != null, (lang) -> {
      return Message.GROUP_PERM_UNSET_NOT_FOUND.substitute(lang, lookup);
    });
    permissionRegister.remove(perm);
    context.respond(Message.GENERIC_LOADING);
    group.update().exceptionally((__) -> 0).thenAccept((res) -> {
      if (res <= 0) {
        context.respond(Message.GROUP_PERM_UNSET_FAIL.substitute(lookup));
        permissionRegister.register(perm); // revert back changes made
      } else
        context.respond(Message.GROUP_PERM_UNSET_SUCCESS.substitute(lookup));
      group.updatePlayers();
    });
    group.push();
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() != 2)
      return super.tabComplete(context, args);
    String filter = args.getString(1);
    @Nullable PerxGroup group = args.get(0).getGroup();
    if (group == null) return List.of();
    return group.getRepository().toPermissionMap().keySet().stream()
        .filter((x) -> StringUtils.startsWithIgnoreCase(x, filter))
        .collect(Collectors.toList());
  }
}
