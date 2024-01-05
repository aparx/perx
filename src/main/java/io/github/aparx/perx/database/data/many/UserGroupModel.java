package io.github.aparx.perx.database.data.many;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.aparx.perx.database.data.DatabaseModel;
import io.github.aparx.perx.database.data.group.GroupModel;

import java.util.Date;
import java.util.UUID;

/**
 * Many-to-many relational table, that uniquely maps a {@code userId} (UUID) and a {@code
 * GroupModel} to each other.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 05:44
 * @since 1.0
 */
@DatabaseTable(tableName = "user_group", daoClass = UserGroupDao.class)
public class UserGroupModel implements DatabaseModel<Long> {

  public static final String USER_ID_FIELD_NAME = "user_id";
  public static final String GROUP_ID_FIELD_NAME = "group_id";

  @DatabaseField(generatedId = true)
  private long id;

  @DatabaseField(uniqueCombo = true, columnName = USER_ID_FIELD_NAME, canBeNull = false)
  private UUID user;

  @DatabaseField(uniqueCombo = true, foreign = true, columnName = GROUP_ID_FIELD_NAME)
  private GroupModel group;

  @DatabaseField
  private Date endDate;

  protected UserGroupModel() {}

  public UserGroupModel(UUID user, GroupModel group) {
    this(user, group, null);
  }

  public UserGroupModel(UUID user, GroupModel group, Date endDate) {
    this.user = user;
    this.group = group;
    this.endDate = endDate;
  }

  public Long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public UUID getUserId() {
    return user;
  }

  public GroupModel getGroup() {
    return group;
  }

  public void setGroup(GroupModel group) {
    this.group = group;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  @Override
  public String toString() {
    return "UserGroupModel{" +
        "id=" + id +
        ", user=" + user +
        ", group=" + group +
        ", endDate=" + endDate +
        '}';
  }
}
