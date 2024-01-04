package io.github.aparx.perx.group.style;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.group.style.GroupStyleKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.scoreboard.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Objects;

/**
 * A default {@code GroupStyleExecutor} implementation, that utilizes a scoreboard to apply style.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 02:43
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class ScoreboardGroupStyleExecutor implements GroupStyleExecutor {

  /** Removes all teams from all groups off the scoreboard */
  public void clear() {
    Perx.getInstance().getGroupController().forEach((group) -> {
      @Nullable Team team = getScoreboard().getTeam(getTeamName(group));
      if (team != null) team.unregister();
    });
  }

  @Override
  @CanIgnoreReturnValue
  public boolean apply(PerxGroup group, Permissible permissible) {
    if (!(permissible instanceof Player player))
      return false;
    Team team = getOrCreateTeam(getScoreboard(), group);
    applyStyleToTeam(team, group); // update team style in case of change
    if (!team.hasEntry(player.getName()))
      team.addEntry(player.getName());
    return true;
  }

  @Override
  @CanIgnoreReturnValue
  public boolean remove(PerxGroup group, Permissible permissible) {
    if (!(permissible instanceof Player player))
      return false;
    @Nullable Team team = getScoreboard().getTeam(getTeamName(group));
    if (team == null || !team.removeEntry(player.getName()))
      return false;
    if (team.getEntries().isEmpty())
      team.unregister();
    return true;
  }

  public void applyStyleToTeam(Team team, PerxGroup group) {
    if (group.hasStyle(GroupStyleKey.PREFIX))
      team.setPrefix(Objects.requireNonNull(group.getStyle(GroupStyleKey.PREFIX)));
    if (group.hasStyle(GroupStyleKey.SUFFIX))
      team.setSuffix(Objects.requireNonNull(group.getStyle(GroupStyleKey.SUFFIX)));
  }

  public Team getOrCreateTeam(Scoreboard scoreboard, PerxGroup group) {
    String teamName = getTeamName(group);
    @Nullable Team team = scoreboard.getTeam(teamName);
    if (team != null) return team;
    team = scoreboard.registerNewTeam(teamName);
    applyStyleToTeam(team, group);
    return team;
  }

  public Scoreboard getScoreboard() {
    return Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
  }

  protected String getTeamName(PerxGroup group) {
    return group.getPriority() + "::perx::" + group.getName();
  }
}
