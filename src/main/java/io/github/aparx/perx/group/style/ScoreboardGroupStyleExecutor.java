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

  private static final String PERX_TEAM_IDENTIFIER = "::perx::";

  /** Removes all teams from all groups off the scoreboard */
  public void clear() {
    getScoreboard().getTeams().forEach((team) -> {
      if (team.getName().contains(PERX_TEAM_IDENTIFIER))
        team.unregister();
    });
  }

  @Override
  @CanIgnoreReturnValue
  @SuppressWarnings("deprecation")
  public boolean apply(PerxGroup group, Permissible permissible) {
    if (!(permissible instanceof Player player))
      return false;
    Team team = getOrCreateTeam(getScoreboard(), group);
    applyStyleToTeam(team, group); // update team style in case of change
    if (!team.hasPlayer(player))
      team.addPlayer(player);
    return true;
  }

  @Override
  @CanIgnoreReturnValue
  @SuppressWarnings("deprecation")
  public boolean reset(PerxGroup group, Permissible permissible) {
    if (!(permissible instanceof Player player))
      return false;
    @Nullable Team team = getScoreboard().getTeam(getTeamName(group));
    if (team == null || !team.removePlayer(player))
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
    return group.getPriority() + PERX_TEAM_IDENTIFIER + group.getName();
  }
}
