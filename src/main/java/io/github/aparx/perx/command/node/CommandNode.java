package io.github.aparx.perx.command.node;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.args.CommandArgumentList;
import io.github.aparx.perx.command.errors.CommandError;
import io.github.aparx.perx.command.errors.CommandAuthorizationError;
import io.github.aparx.perx.command.errors.CommandSyntaxError;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 10:40
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class CommandNode implements CommandNodeExecutor, Iterable<CommandNode> {

  private final CommandNode root;
  private final @Nullable CommandNode parent;
  private final CommandNodeInfo info;
  private final int index;
  private final ArrayPath fullPath;
  private final Supplier<String> fullUsage = Suppliers.memoize(() -> createFullUsage(this));

  private final Map<String, CommandNode> children = new LinkedHashMap<>();

  public CommandNode(CommandNodeInfo info) {
    this(null, info);
  }

  public CommandNode(@Nullable CommandNode parent, CommandNodeInfo info) {
    Preconditions.checkNotNull(info, "Info must not be null");
    this.root = (parent != null ? parent.root : this);
    this.parent = parent;
    this.info = info;
    this.fullPath = createPath(this);
    this.index = fullPath.length() - 1;
  }

  protected static String createNameKey(CommandNodeInfo info) {
    return createNameKey(info.name());
  }

  protected static String createNameKey(String nodeName) {
    return nodeName.toLowerCase(Locale.ENGLISH);
  }

  private static ArrayPath createPath(CommandNode leaf) {
    return createPath(leaf, new ArrayList<>());
  }

  private static ArrayPath createPath(CommandNode node, List<String> pathList) {
    pathList.add(0, node.getInfo().name());
    @Nullable CommandNode parent = node.getParent();
    if (parent != null && node.hasParent())
      return createPath(parent, pathList);
    return ArrayPath.of(pathList.toArray(String[]::new));
  }

  private static String createFullUsage(CommandNode leafNode) {
    StringBuilder builder = new StringBuilder();
    createCommandString(leafNode, builder);
    CommandNodeInfo info = leafNode.getInfo();
    if (info.hasUsage()) {
      if (!builder.isEmpty()) builder.append(CommandArgumentList.ARGUMENT_SEPARATOR);
      builder.append(info.usage());
    }
    return builder.toString();
  }

  private static String createCommandString(CommandNode node, StringBuilder builder) {
    if (!builder.isEmpty())
      builder.insert(0, CommandArgumentList.ARGUMENT_SEPARATOR);
    builder.insert(0, node.getInfo().name());
    @Nullable CommandNode parent = node.getParent();
    if (parent != null && node.hasParent())
      return createCommandString(parent, builder);
    return builder.toString();
  }

  @SuppressWarnings("Guava")
  private static Predicate<CommandNode> createNameFilterPredicate(@Nullable String name) {
    if (name == null) return Predicates.alwaysTrue();
    return (node) -> StringUtils.startsWithIgnoreCase(node.getInfo().name(), name);
  }

  public CommandNode getRoot() {
    return root;
  }

  public @Nullable CommandNode getParent() {
    return parent;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public CommandNodeInfo getInfo() {
    return info;
  }

  public ArrayPath getFullPath() {
    return fullPath;
  }

  public int getPathIndex() {
    return index;
  }

  public String getFullUsage() {
    return fullUsage.get();
  }

  /**
   * Adds {@code child} as a child to this node, if it is not already registered.
   *
   * @param child the child to add to this node
   * @return this node instance, to be able to chain multiple calls
   * @throws NullPointerException     if {@code child} is null
   * @throws IllegalArgumentException if either the child's parent is not equal to this node, or
   *                                  it's root differs from this node's root
   */
  @CanIgnoreReturnValue
  public CommandNode addChild(CommandNode child) {
    Preconditions.checkNotNull(child, "Child must not be null");
    Preconditions.checkArgument(equals(child.getParent()), "Parent mismatch");
    Preconditions.checkArgument(getRoot().equals(child.getRoot()), "Root mismatch");
    if (children.putIfAbsent(createNameKey(child.getInfo()), child) != null)
      throw new IllegalArgumentException("Child is already registered");
    return this;
  }

  /** @see #addChild(CommandNode) */
  @CanIgnoreReturnValue
  public CommandNode addChild(Function<CommandNode, CommandNode> childFactory) {
    // This is a convenience method for chaining children deeply
    return addChild(childFactory.apply(this));
  }

  /** @see #addChild(CommandNode) */
  @CanIgnoreReturnValue
  public CommandNode addChildren(CommandNode first, CommandNode... others) {
    addChild(first);
    if (ArrayUtils.isNotEmpty(others))
      for (CommandNode other : others)
        addChild(other);
    return this;
  }

  public @Nullable CommandNode getChild(String name) {
    return children.get(createNameKey(name));
  }

  public boolean isAuthorized(Permissible permissible) {
    CommandNodeInfo info = getInfo();
    ImmutableList<String> permissions = info.permissions();
    if (info.hasPermission())
      return permissions.stream().anyMatch(permissible::hasPermission);
    return true;
  }

  /**
   * Executes this node using given {@code context} and (relative) arguments.
   *
   * @param context the initial execution context
   * @param args    the arguments relative to this node
   * @throws CommandError if the command cannot be executed or finished properly
   * @apiNote The default implementation tries to cascade the execution to the next child that is
   * matching the first argument and throws an {@code CommandSyntaxError} if that cannot properly
   * succeed.
   */
  @Override
  public void execute(CommandContext context, CommandArgumentList args) throws CommandError {
    tryCascadeExecute(context, args); // cascades execution down to matching child(ren)
  }

  @Override
  public @Nullable List<String> tabComplete(CommandContext context, CommandArgumentList args) {
    CommandSender sender = context.sender();
    if (args.isEmpty() || !isAuthorized(sender))
      return List.of();
    @Nullable CommandNode child = getChild(args.getString(0));
    if (child != null && !child.isAuthorized(sender))
      return List.of();
    @Nullable List<String> strings = (child != null
        ? child.tabComplete(context, args.skip())
        : null);
    if (strings != null) return strings;
    return getCompletingChildren(context, createNameFilterPredicate(args.getString(0)));
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    CommandNode that = (CommandNode) object;
    return Objects.equals(parent, that.parent)
        && Objects.equals(info, that.info);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parent, info);
  }

  protected void tryCascadeExecute(CommandContext context, CommandArgumentList args) throws CommandError {
    if (args.isEmpty()) throw createSyntaxError(context);
    @Nullable CommandNode child = getChild(args.first().value());
    if (child == null) throw createSyntaxError(context);
    child.execute(context, args.skip());
  }

  protected CommandError createAuthorizationError() {
    return new CommandAuthorizationError(this);
  }

  protected CommandError createAuthorizationError(String permission) {
    return new CommandAuthorizationError(permission);
  }

  protected CommandError createSyntaxError(CommandContext context) {
    return new CommandSyntaxError(context, this);
  }

  protected @Nullable List<String> getCompletingChildren(
      CommandContext context, Predicate<CommandNode> nodeFilter) {
    return children.values().stream()
        .filter(nodeFilter)
        .filter((node) -> node.isAuthorized(context.sender()))
        .map((node) -> node.getInfo().name())
        .collect(Collectors.toList());
  }

  protected Stream<? extends Player> getCompletingPlayerStream(
      CommandContext context, @Nullable String name) {
    Stream<? extends Player> stream = Bukkit.getOnlinePlayers().stream();
    if (context.isPlayer())
      stream = stream.filter(context.getPlayer()::canSee);
    if (StringUtils.isNotEmpty(name))
      stream = stream.filter((p) -> StringUtils.startsWithIgnoreCase(p.getName(), name));
    return stream;
  }

  protected List<String> getCompletingPlayers(CommandContext context, @Nullable String name) {
    return getCompletingPlayerStream(context, name).map(Player::getName).collect(Collectors.toList());
  }

  @Override
  public Iterator<CommandNode> iterator() {
    return children.values().iterator();
  }

}
