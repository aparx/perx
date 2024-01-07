package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.PerxCommand;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.permission.PerxPermission;
import io.github.aparx.perx.permission.PerxPermissionRegister;
import io.github.aparx.perx.utils.pagination.BasicPageContainer;
import io.github.aparx.perx.utils.pagination.PageContainerDecorator;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Locale;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 12:11
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupInfoCommand extends AbstractGroupCommand {

  private static final int MAX_LINES_PER_PAGE = 9;

  public GroupInfoCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("info")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .description("Shows information about a group")
        .usage("<Group> (Page)")
        .build());
  }

  @Override
  protected void execute(CommandContext context, CommandArgumentList args, PerxGroup group) throws CommandError {
    if (args.length() > 1) throw createSyntaxError(context);
    int pageIndex = (!args.isEmpty() ? args.get(0).getInt() - 1 : 0);
    PageContainerDecorator<String, List<String>> pages = createPages(group);
    BasicPageContainer<List<String>> pageContainer = pages.getContainer();
    CommandAssertion.checkInRange(1 + pageIndex, 1, pageContainer.size());
    String line = ChatColor.GRAY + "-".repeat(10) + ChatColor.YELLOW;
    context.respond(String.format("%s %s Group: %s %s/%s %s", Message.PREFIX, line,
        group.getName(), 1 + pageIndex, pageContainer.size(), line));
    pageContainer.getPage(pageIndex).forEach(context::respond);
  }

  private PageContainerDecorator<String, List<String>> createPages(PerxGroup group) {
    PageContainerDecorator<String, List<String>> pages =
        PageContainerDecorator.of(MAX_LINES_PER_PAGE);
    for (GroupStyleKey key : GroupStyleKey.values())
      pages.addElement(createKeyValueLine(
          StringUtils.capitalize(key.name().toLowerCase(Locale.ENGLISH)),
          group.getStyle(key)));
    PerxPermissionRegister permissions = group.getPermissions();
    int size = permissions.size();
    pages.addElement(createKeyValueLine("Default", group.isDefault()));
    pages.addElement(createKeyValueLine("Priority", group.getPriority()));
    pages.addElement(createKeyValueLine("Permissions (" + size + ")", StringUtils.EMPTY));
    for (PerxPermission permission : permissions)
      pages.addElement(createPermissionLine(permission));
    return pages;
  }

  private String createKeyValueLine(String key, @Nullable Object value) {
    return String.valueOf(Message.PREFIX) +
        ChatColor.DARK_GRAY +
        " • " +
        ChatColor.GRAY +
        key +
        ": " +
        ChatColor.RESET +
        (value == null ? '-' : value);
  }

  private String createPermissionLine(PerxPermission permission) {
    boolean value = permission.getValue();
    return String.valueOf(Message.PREFIX)
        + ChatColor.DARK_GRAY
        + "   " + (value ? '+' : '-') + ' '
        + ChatColor.WHITE
        + permission.getName()
        + (value ? ChatColor.GRAY : ChatColor.RED)
        + " ("
        + value
        + ')';
  }
}
