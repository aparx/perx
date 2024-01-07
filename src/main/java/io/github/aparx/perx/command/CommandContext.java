package io.github.aparx.perx.command;

import com.google.common.base.Preconditions;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * The initial context of a command execution, which contains information about the executor, the
 * initial arguments and thus a probable intention.
 *
 * @param sender    the sender, that invoked a command execution
 * @param arguments the initial arguments passed by the executor
 * @param label     the label with which the invocation was started
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:36
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public record CommandContext(
    CommandSender sender,
    CommandArgumentList arguments,
    String label
) {

  public CommandContext {
    Preconditions.checkNotNull(sender, "Sender must not be null");
    Preconditions.checkNotNull(arguments, "Arguments must not be null");
    Preconditions.checkNotNull(label, "Label must not be null");
  }

  public Player getPlayer() {
    if (!(sender instanceof Player player))
      throw new IllegalArgumentException("Sender is not a player");
    return player;
  }

  public boolean isPlayer() {
    return (sender instanceof Player);
  }

  public boolean hasArguments() {
    return !arguments.isEmpty();
  }

  /**
   * Response to the action taken by this sender.
   * <p>The response sends a message to the sender, if it is possible.
   *
   * @param message the message to represent a response
   * @apiNote This is a better usage than sending the message directly to the sender, since it
   * centralized and also takes into account time of the response. For example, if a message is
   * sent directly but asynchronously, and the sender is not able to receive responses anymore,
   * it would break the code.
   */
  public void respond(String message) {
    if (!isPlayer() || getPlayer().isOnline())
      sender.sendMessage(message);
  }

  /** @see #respond(String)  */
  public void respond(Message key) {
    respond(key.substitute());
  }

}
