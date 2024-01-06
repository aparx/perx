package io.github.aparx.perx.database.data.group;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.aparx.perx.database.data.DatabaseModel;
import io.github.aparx.perx.group.PerxGroup;
import org.bukkit.permissions.PermissionDefault;

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

  private static String[] stringToPerms(String string) {
    return (string != null ? GSON.fromJson(string, String[].class) : new String[0]);
  }

  private static String permsToString(String[] permissions) {
    return (permissions != null ? GSON.toJson(permissions) : "[]");
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

  public String[] getPermissions() {
    return stringToPerms(permissions);
  }

  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }

  public void setPermissions(String[] permissions) {
    setPermissions(permsToString(permissions));
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
