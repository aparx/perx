package io.github.aparx.perx.database.data.many;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.aparx.perx.database.data.group.GroupModel;

import java.util.UUID;

/**
 * Many-to-many relational table, that uniquely maps a {@code userId} (UUID) and a {@code
 * GroupModel} to each other.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 05:44
 * @since 1.0
 */
@DatabaseTable(tableName = "user_group", daoClass = UserGroupDAO.class)
public class UserGroup {

  public static final String USER_ID_FIELD_NAME = "user_id";
  public static final String GROUP_ID_FIELD_NAME = "group_id";

  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(uniqueCombo = true, columnName = USER_ID_FIELD_NAME)
  private UUID user;

  @DatabaseField(uniqueCombo = true, foreign = true, columnName = GROUP_ID_FIELD_NAME)
  private GroupModel group;

  protected UserGroup() {}

  public UserGroup(UUID user, GroupModel group) {
    this.user = user;
    this.group = group;
  }

  public int getId() {
    return id;
  }

  public UUID getUser() {
    return user;
  }

  public GroupModel getGroup() {
    return group;
  }
}
