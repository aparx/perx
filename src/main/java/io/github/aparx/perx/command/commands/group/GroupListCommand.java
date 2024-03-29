package io.github.aparx.perx.command.commands.group;

import io.github.aparx.perx.Perx;
import io.github.aparx.perx.PerxPermissions;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.PerxGroupService;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.utils.pagination.BasicPageContainer;
import io.github.aparx.perx.utils.pagination.PageContainerDecorator;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 14:16
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class GroupListCommand extends CommandNode {

  private static final int MAX_GROUPS_PER_PAGE = 10;

  public GroupListCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder("list")
        .permission(PerxPermissions.PERMISSION_MANAGE)
        .usage("(Page)")
        .description("Shows all groups")
        .build());
  }

  @Override
  public void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.length() > 1) throw createSyntaxError(context);
    int pageIndex = (!args.isEmpty() ? args.get(0).getInt() - 1 : 0);
    PageContainerDecorator<PerxGroup, List<PerxGroup>> pages = createPages();
    BasicPageContainer<List<PerxGroup>> pageContainer = pages.getContainer();
    if (pageContainer.isEmpty())
      throw new CommandError(Message.GENERIC_GROUP_NONE_EXISTING.substitute());
    CommandAssertion.checkInRange(1 + pageIndex, 1, pageContainer.size());
    List<PerxGroup> page = pageContainer.getPage(pageIndex);
    String line = ChatColor.GRAY + "-".repeat(3) + ChatColor.YELLOW;
    StringBuilder builder = new StringBuilder();
    String prefix = Message.PREFIX.toString();
    builder.append(prefix)
        .append(' ')
        .append(line)
        .append(' ')
        .append(1 + pageIndex)
        .append('/')
        .append(pageContainer.size())
        .append(' ')
        .append(line)
        .append('\n');
    page.forEach((group) -> builder
        .append(prefix)
        .append(' ')
        .append(ChatColor.GRAY)
        .append("• ")
        .append(group.getName())
        .append(group.isDefault() ? " (default)" : StringUtils.EMPTY)
        .append('\n'));
    context.respond(builder.toString());
  }

  private PageContainerDecorator<PerxGroup, List<PerxGroup>> createPages() {
    PageContainerDecorator<PerxGroup, List<PerxGroup>> pages =
        PageContainerDecorator.of(MAX_GROUPS_PER_PAGE);
    Perx.getInstance().getGroupService().getRepository().forEach(pages::addElement);
    return pages;
  }
}
