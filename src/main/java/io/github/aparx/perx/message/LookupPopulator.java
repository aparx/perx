package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.CommandContext;
import io.github.aparx.perx.command.node.CommandNode;
import io.github.aparx.perx.command.node.CommandNodeInfo;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.ArrayPath;
import io.github.aparx.perx.utils.duration.DurationUtils;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 08:47
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class LookupPopulator {

  private final Map<ArrayPath, @Nullable Object> valueMap;
  private final List<StringLookup> lookups = new ArrayList<>();

  private final StringLookup stringLookup;

  public LookupPopulator() {
    this(new HashMap<>());
  }

  public LookupPopulator(Map<ArrayPath, Object> map) {
    Preconditions.checkNotNull(map, "Map must not be null");
    Preconditions.checkState(!map.containsKey(null), "Must not contain null keys");
    this.valueMap = map;
    this.stringLookup = createLookup(valueMap, lookups);
  }

  protected static StringLookup createLookup(
      Map<ArrayPath, @Nullable Object> map, List<StringLookup> lookups) {
    Preconditions.checkNotNull(map, "Map must not be null");
    Preconditions.checkNotNull(lookups, "Lookups must not be null");
    return (variableName) -> {
      for (StringLookup lookup : lookups) {
        String val = lookup.lookup(variableName);
        if (val != null) return val;
      }
      ArrayPath path = ArrayPath.parse(variableName);
      if (map.containsKey(path)) {
        @Nullable Object o = map.get(path);
        if (o instanceof Supplier<?> supplier)
          o = supplier.get();
        o = Objects.toString(o, null);
        if (o != null) return (String) o;
      }
      return null;
    };
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(String path, @Nullable String value) {
    return put(ArrayPath.parse(path), value);
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(String path, Supplier<@Nullable String> supplier) {
    return put(ArrayPath.parse(path), supplier);
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath path, @Nullable String value) {
    Preconditions.checkNotNull(path, "Path must not be null");
    valueMap.put(path, value);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath path, Supplier<@Nullable String> supplier) {
    Preconditions.checkNotNull(path, "Path must not be null");
    valueMap.put(path, supplier);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable OfflinePlayer player) {
    if (player == null) return this;
    @Nullable String name = player.getName();
    if (name != null) put(prefix.add("name"), name);
    put(prefix.add("uuid"), player.getUniqueId().toString());
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable Player player) {
    if (player == null) return this;
    put(prefix, (OfflinePlayer) player);
    put(prefix.add("displayName"), player.getDisplayName()); // TODO test with group
    put(prefix.add("tabListName"), player.getPlayerListName());
    PerxUserController userController = Perx.getInstance().getUserController();
    @Nullable PerxUser user = userController.get(player);
    if (user != null)
      user.getHighestUserGroup().ifPresent((userGroup) -> {
        put(prefix.add("group"), userGroup);
        @Nullable PerxGroup group = userGroup.findGroup();
        if (group != null)
          put(prefix.add("displayName"), group.createCustomName(player.getName()));
      });
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable PerxUserGroup userGroup) {
    if (userGroup == null) return this;
    // TODO add (lazy) time left, date etc.
    @Nullable PerxGroup group = userGroup.findGroup();
    if (group != null) put(prefix, group);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable PerxGroup group) {
    return put(prefix, group, null);
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable PerxGroup group, @Nullable String nil) {
    if (group == null) return this;
    put(prefix.add("name"), group.getName());
    put(prefix.add("priority"), String.valueOf(group.getPriority()));
    put(prefix.add("default"), String.valueOf(group.isDefault()));
    put(prefix.add("default").add("color"), String.valueOf(
        group.isDefault() ? ChatColor.GREEN : ChatColor.RED));
    for (GroupStyleKey key : GroupStyleKey.values()) {
      @Nullable String style = group.getStyle(key);
      put(prefix.add(key.name().toLowerCase()), Objects.toString(style, nil));
    }
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable CommandContext context) {
    if (context == null) return this;
    if (context.isPlayer())
      put(prefix.add("player"), context.getPlayer());
    put(prefix.add("label"), context.label());
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable CommandNodeInfo info) {
    return put(prefix, info, null);
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable CommandNodeInfo info,
                             @Nullable String nil) {
    if (info == null) return this;
    put(prefix.add("name"), info.name());
    put(prefix.add("usage"), Objects.toString(info.usage(), nil));
    put(prefix.add("description"), Objects.toString(info.description(), nil));
    put(prefix.add("permissions"), String.join(", ", info.permissions()));
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable CommandNode node) {
    return put(prefix, node, null);
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable CommandNode node, @Nullable String nil) {
    if (node == null) return this;
    put(prefix, node.getInfo(), nil);
    put(prefix.add("fullUsage"), node::getFullUsage);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath prefix, @Nullable Duration duration) {
    if (duration == null) return this;
    // TODO implementation
    put(prefix, Suppliers.memoize(() -> DurationUtils.createTimeLeft(duration)));
    put(prefix.add("millis"), String.valueOf(duration.toMillis()));
    put(prefix.add("seconds"), String.valueOf(duration.toSeconds()));
    put(prefix.add("hours"), String.valueOf(duration.toHours()));
    put(prefix.add("days"), String.valueOf(duration.toDays()));
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator lookup(StringLookup lookup) {
    Preconditions.checkNotNull(lookup, "Lookup must not be null");
    lookups.add(lookup);
    return this;
  }

  public StringLookup getLookup() {
    return stringLookup;
  }
}
