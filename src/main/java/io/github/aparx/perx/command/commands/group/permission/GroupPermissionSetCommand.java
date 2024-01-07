package io.github.aparx.perx.command.commands.group.permission;

import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.commands.group.AbstractGroupCommand;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.message.LookupPopulator;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.permission.PerxPermission;
import io.github.aparx.perx.permission.PerxPermissionRegister;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 03:35
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupPermissionSetCommand extends AbstractGroupCommand {

  private final List<String> permissions;

  public GroupPermissionSetCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("set")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Set a permission to a group (true: give, false: revoke)")
        .usage("<Group> <Permission> (true:false)")
        .build());
    permissions = getPermissionFromAllPlugins();
    permissions.add(0, "*");
  }

  public static List<String> getPermissionFromAllPlugins() {
    return Arrays.stream(Bukkit.getPluginManager().getPlugins())
        .map((plugin) -> plugin.getDescription().getPermissions())
        .filter(Predicate.not(List::isEmpty))
        .flatMap(List::stream)
        .map(Permission::getName)
        .collect(Collectors.toList());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (args.isEmpty() || args.length() > 2) throw createSyntaxError(context);
    String permission = args.getString(0);
    boolean value = (args.length() != 2 || args.get(1).getBoolean());
    ArrayPath arrayPath = ArrayPath.parse(permission);
    PerxPermissionRegister permissionRegister = group.getPermissions();
    @Nullable PerxPermission before = permissionRegister.get(arrayPath);
    @Nullable Boolean previousValue = (before != null ? before.getValue() : null);
    PerxPermission updated = group.getPermissions().set(permission, value);
    context.respond(Message.GENERIC_LOADING);
    group.update().exceptionally((__) -> 0).thenAccept((res) -> {
      StringLookup lookup = new LookupPopulator()
          .put(ArrayPath.of("group"), group)
          .put(ArrayPath.of("perm"), updated)
          .getLookup();
      if (res <= 0) {
        context.respond(Message.GROUP_PERM_SET_FAIL.substitute(lookup));
        // revert back changes made
        if (previousValue == null) permissionRegister.remove(arrayPath);
        else permissionRegister.set(arrayPath, previousValue);
      } else
        context.respond(Message.GROUP_PERM_SET_SUCCESS.substitute(lookup));
      group.updatePlayers();
    });
    group.push();
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    if (args.length() == 3) return List.of("true", "false");
    if (args.length() == 2)
      return permissions.stream()
          .filter((x) -> StringUtils.startsWithIgnoreCase(x, args.getString(1)))
          .collect(Collectors.toList());
    return super.tabComplete(context, args);
  }
}
