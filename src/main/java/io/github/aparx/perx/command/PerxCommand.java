package io.github.aparx.perx.command;

import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.commands.GroupCommand;
import io.github.aparx.perx.command.commands.HelpCommand;
import io.github.aparx.perx.command.commands.InfoCommand;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 10:52
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxCommand implements CommandExecutor, TabCompleter {

  private static final PerxCommand instance = new PerxCommand();

  private final CommandNode root =
      new CommandNode(CommandNodeInfo.builder("perx").usage("help").build());

  private PerxCommand() {
    root.addChild(HelpCommand::new);
    root.addChild(InfoCommand::new);
    root.addChild(GroupCommand::new);
  }

  public static PerxCommand getInstance() {
    return instance;
  }

  public static CommandNode getTree() {
    return instance.root;
  }

  public CommandNode getRoot() {
    return root;
  }

  @Override
  public boolean onCommand(
      CommandSender sender, Command command, String label, String[] args) {
    try {
      CommandArgumentList compiled = CommandArgumentList.of(args);
      root.execute(new CommandContext(sender, compiled, label), compiled);
    } catch (CommandError e) {
      // Catching commands like this, we allow for simpler implementation and also for
      // a multi-language project if we wanted to (by simply using a different register).
      @Nullable String localizedMessage = e.getLocalizedMessage();
      if (localizedMessage == null)
        throw new RuntimeException(e);
      sender.sendMessage(localizedMessage);
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String label, String[] args) {
    CommandArgumentList compiled = CommandArgumentList.of(args);
    return root.tabComplete(new CommandContext(sender, compiled, label), compiled);
  }
}
