package io.github.aparx.perx.command.commands;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import io.github.aparx.perx.command.CommandAssertion;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.message.Message;
import io.github.aparx.perx.utils.pagination.BasicPageContainer;
import io.github.aparx.perx.utils.pagination.PageContainerDecorator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:43
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class HelpCommand extends CommandNode {

  private static final int MAX_NODES_PER_PAGE = 4;

  private final Supplier<ImmutableList<CommandNode>> descriptiveNodes = Suppliers.memoize(() -> {
    ImmutableList.Builder<CommandNode> listBuilder = ImmutableList.builder();
    collectDescriptiveNodes(getRoot(), listBuilder);
    return listBuilder.build();
  });

  public HelpCommand(CommandNode parent) {
    super(parent, CommandNodeInfo.builder()
        .name("help")
        .usage("(page)")
        .description("Shows you a list of commands Perx offers")
        .build());
  }

  private static void collectDescriptiveNodes(
      CommandNode node, ImmutableList.Builder<CommandNode> output) {
    if (node.getInfo().hasDescription()) output.add(node);
    node.forEach((child) -> collectDescriptiveNodes(child, output));
  }

  @Override
  public void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.length() > 1)
      throw createSyntaxError(context);
    final int pageIndex = (!args.isEmpty() ? args.get(0).getInt() - 1 : 0);
    CommandSender sender = context.sender();
    PageContainerDecorator<CommandNode, List<CommandNode>> pages = createPages(sender);
    BasicPageContainer<List<CommandNode>> pageContainer = pages.getContainer();
    CommandAssertion.checkInRange(1 + pageIndex, 1, pageContainer.size());
    List<CommandNode> displayingPage = pageContainer.getPage(pageIndex);
    String line = ChatColor.GRAY + "-".repeat(15) + ChatColor.YELLOW;
    StringBuilder builder = new StringBuilder();
    builder.append(Message.PREFIX)
        .append(' ')
        .append(line)
        .append(" Perx help ")
        .append(1 + pageIndex)
        .append('/')
        .append(pageContainer.size())
        .append(' ')
        .append(line)
        .append('\n');
    displayingPage.forEach((node) -> {
      builder
          .append(Message.PREFIX)
          .append(ChatColor.GRAY)
          .append(" •")
          .append(ChatColor.WHITE)
          .append(" /")
          .append(node.getFullUsage())
          .append('\n');
      builder
          .append(Message.PREFIX)
          .append(ChatColor.GRAY)
          .append("   ")
          .append(node.getInfo().description())
          .append('\n');
    });
    context.respond(builder.toString());
  }

  private PageContainerDecorator<CommandNode, List<CommandNode>> createPages(CommandSender sender) {
    PageContainerDecorator<CommandNode, List<CommandNode>> pages =
        PageContainerDecorator.of(MAX_NODES_PER_PAGE);
    pages.addElements(descriptiveNodes.get().stream()
        .filter((node) -> node.isAuthorized(sender))
        .collect(Collectors.toList()));
    return pages;
  }
}
