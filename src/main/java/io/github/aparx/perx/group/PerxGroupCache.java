package io.github.aparx.perx.group;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-07 07:03
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class PerxGroupCache implements PerxGroupRepository {

  private final Map<String, PerxGroup> groupMap = new ConcurrentHashMap<>();

  @Override
  public int size() {
    return groupMap.size();
  }

  @Override
  public boolean register(PerxGroup group) {
    return groupMap.putIfAbsent(group.getName(), group) == null;
  }

  @Override
  public @Nullable PerxGroup put(PerxGroup group) {
    return groupMap.put(group.getName(), group);
  }

  @Override
  public boolean remove(PerxGroup group) {
    return groupMap.remove(group.getName(), group);
  }

  @Override
  public boolean remove(String name) {
    return groupMap.remove(PerxGroup.transformKey(name)) != null;
  }

  @Override
  public boolean contains(PerxGroup group) {
    return group.equals(groupMap.get(group.getName()));
  }

  @Override
  public boolean contains(String name) {
    return groupMap.containsKey(PerxGroup.transformKey(name));
  }

  @Override
  public @Nullable PerxGroup get(String name) {
    return groupMap.get(PerxGroup.transformKey(name));
  }

  @Override
  public Iterator<PerxGroup> iterator() {
    return groupMap.values().iterator();
  }
}
