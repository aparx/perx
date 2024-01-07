package io.github.aparx.perx.group.style;

import com.google.common.collect.AbstractIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.github.aparx.perx.Perx;
import io.github.aparx.perx.command.commands.group.AbstractGroupCommand;
import io.github.aparx.perx.group.PerxGroup;
import io.github.aparx.perx.group.style.GroupStyleExecutor;
import io.github.aparx.perx.group.style.GroupStyleKey;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.scoreboard.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.Iterator;
import java.util.Objects;

/**
 * A default {@code GroupStyleExecutor} implementation, that utilizes a scoreboard to apply style.
 *
 * @author aparx (Vinzent Z.)
 * @version 2024-01-04 02:43
 * @since 1.0
 */
@DefaultQualifier(NonNull.class)
public class ScoreboardGroupStyleExecutor implements GroupStyleExecutor, Iterable<Team> {

  private static final String PERX_TEAM_IDENTIFIER = "::perx::";

  /** Removes all teams from all groups off the scoreboard */
  public void clear() {
    forEach(Team::unregister);
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

  @Override
  @SuppressWarnings("deprecation")
  public void resetAll(Permissible permissible) {
    if (!(permissible instanceof Player player)) return;
    forEach((team) -> team.removePlayer(player));
  }

  @Override
  public String createDisplayName(PerxGroup group, String playerName) {
    StringBuilder builder = new StringBuilder();
    if (group.hasStyle(GroupStyleKey.PREFIX))
      builder.append(group.getStyle(GroupStyleKey.PREFIX));
    builder.append(ChatColor.RESET);
    builder.append(playerName);
    if (group.hasStyle(GroupStyleKey.SUFFIX))
      builder.append(group.getStyle(GroupStyleKey.SUFFIX));
    builder.append(ChatColor.RESET);
    return builder.toString();
  }

  public void applyStyleToTeam(Team team, PerxGroup group) {
    team.setPrefix(Objects.toString(group.getStyle(GroupStyleKey.PREFIX), StringUtils.EMPTY));
    team.setSuffix(Objects.toString(group.getStyle(GroupStyleKey.SUFFIX), StringUtils.EMPTY));
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

  @Override
  public Iterator<Team> iterator() {
    Iterator<Team> iterator = getScoreboard().getTeams().iterator();
    return new AbstractIterator<>() {
      @Nullable
      @Override
      protected Team computeNext() {
        if (!iterator.hasNext())
          return endOfData();
        @Nullable Team next = iterator.next();
        if (!next.getName().contains(PERX_TEAM_IDENTIFIER))
          next = computeNext();
        if (next == null) endOfData(); // ensure
        return next;
      }
    };
  }
}
