package io.github.aparx.perx.message;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleKey;
import io.github.aparx.perx.group.union.PerxUserGroup;
import io.github.aparx.perx.user.PerxUser;
import io.github.aparx.perx.user.controller.PerxUserController;
import io.github.aparx.perx.utils.ArrayPath;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-05 08:47
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class LookupPopulator {

  private final Map<ArrayPath, Object> valueMap;
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
      Map<ArrayPath, Object> map, List<StringLookup> lookups) {
    Preconditions.checkNotNull(map, "Map must not be null");
    Preconditions.checkNotNull(lookups, "Lookups must not be null");
    return (variableName) -> {
      for (StringLookup lookup : lookups) {
        String val = lookup.lookup(variableName);
        if (val != null) return val;
      }
      ArrayPath path = ArrayPath.parse(variableName);
      if (map.containsKey(path)) {
        Object o = map.get(path);
        if (o instanceof Supplier<?> supplier)
          o = supplier.get();
        o = Objects.toString(o, null);
        if (o != null) return (String) o;
      }
      return null;
    };
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath path, String value) {
    Preconditions.checkNotNull(path, "Path must not be null");
    valueMap.put(path, value);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator put(ArrayPath path, Supplier<String> supplier) {
    Preconditions.checkNotNull(path, "Path must not be null");
    valueMap.put(path, supplier);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator populatePlayer(ArrayPath prefix, OfflinePlayer player) {
    @Nullable String name = player.getName();
    if (name != null) put(prefix.add("name"), name);
    put(prefix.add("uuid"), player.getUniqueId().toString());
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator populatePlayer(ArrayPath prefix, Player player) {
    populatePlayer(prefix, (OfflinePlayer) player);
    put(prefix.add("displayName"), player.getDisplayName()); // TODO test with group
    put(prefix.add("tabListName"), player.getPlayerListName());
    PerxUserController userController = Perx.getInstance().getUserController();
    @Nullable PerxUser user = userController.get(player);
    if (user != null)
      user.getHighestUserGroup().ifPresent((userGroup) -> {
        populateGroup(prefix.add("group"), userGroup);
        @Nullable PerxGroup group = userGroup.findGroup();
        if (group != null)
          put(prefix.add("displayName"), group.createCustomName(player.getName()));
      });
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator populateGroup(ArrayPath prefix, PerxUserGroup userGroup) {
    // TODO add (lazy) time left, date etc.
    @Nullable PerxGroup group = userGroup.findGroup();
    if (group != null) populateGroup(prefix, group);
    return this;
  }

  @CanIgnoreReturnValue
  public LookupPopulator populateGroup(ArrayPath prefix, PerxGroup group) {
    put(prefix.add("name"), group.getName());
    put(prefix.add("priority"), String.valueOf(group.getPriority()));
    for (GroupStyleKey key : GroupStyleKey.values()) {
      @Nullable String style = group.getStyle(key);
      if (style != null) put(prefix.add(key.name().toLowerCase()), style);
    }
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
