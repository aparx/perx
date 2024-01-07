package io.github.aparx.perx.database.data.group;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.aparx.perx.database.data.DatabaseModel;
import io.github.aparx.perx.group.PerxGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 00:36
 * @since 1.0
 */
@DatabaseTable(tableName = "perm_group")
public class GroupModel implements DatabaseModel<String> {

  private static final Gson GSON = new Gson();

  @DatabaseField(id = true)
  private String id;

  /** The lower the priority, the more important this group is */
  @DatabaseField
  private int priority = PerxGroup.DEFAULT_PRIORITY;

  @DatabaseField
  private String prefix;

  @DatabaseField
  private String suffix;

  @DatabaseField
  private boolean isDefault;

  /** All permissions as a JSON string */
  @DatabaseField(canBeNull = false)
  private String permissions;

  protected GroupModel() {}

  public GroupModel(String id) {
    Preconditions.checkNotNull(id, "ID must not be null");
    this.id = id;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Boolean> stringToPermMap(String string) {
    return (string != null ? GSON.fromJson(string, Map.class) : new HashMap<>());
  }

  private static String permMapToString(Map<String, Boolean> map) {
    return (map != null ? GSON.toJson(map) : "[]");
  }

  @Override
  public String getId() {
    return id;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Map<String, Boolean> getPermissions() {
    return stringToPermMap(permissions);
  }

  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }

  public void setPermissions(Map<String, Boolean> permissions) {
    setPermissions(permMapToString(permissions));
  }

  @Override
  public String toString() {
    return "GroupModel{" +
        "id='" + id + '\'' +
        ", priority=" + priority +
        ", prefix='" + prefix + '\'' +
        ", suffix='" + suffix + '\'' +
        ", defaultValue=" + isDefault +
        ", permissions='" + permissions + '\'' +
        '}';
  }
}
