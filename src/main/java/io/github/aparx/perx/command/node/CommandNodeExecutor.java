package io.github.aparx.perx.command.node;

import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;

/**
 * Functional interface that represents the executor of a command for a given set of inputs.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-06 02:36
 * @since 1.0
 */
@FunctionalInterface
@DefaultQualifier(NonNull.class)
public interface CommandNodeExecutor {

  void execute(CommandContext context, CommandArgumentList args) throws CommandError;

  default @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    return null;
  }

}
