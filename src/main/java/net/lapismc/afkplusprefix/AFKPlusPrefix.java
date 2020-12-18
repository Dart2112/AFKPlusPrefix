package net.lapismc.afkplusprefix;

import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.UUID;

public final class AFKPlusPrefix extends JavaPlugin implements Listener {

    private Scoreboard board;
    private Team afkTeam;
    private final ArrayList<UUID> afkPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        generateTeam();
        if (getConfig().getBoolean("CompatibilityMode")) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (UUID uuid : afkPlayers) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null)
                        continue;
                    if (p.getScoreboard() != board) {
                        p.setScoreboard(board);
                    }
                    if (!afkTeam.hasEntry(p.getName()))
                        afkTeam.addEntry(p.getName());
                }
            }, 5, 5);
        }
    }

    private void enableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        if (getConfig().getBoolean("CompatibilityMode") && p.getScoreboard() != board) {
            p.setScoreboard(board);
        }
        board.getTeam("AFK").addEntry(p.getName());
        afkPlayers.add(uuid);
    }

    private void disableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        board.getTeam("AFK").removeEntry(p.getName());
        afkPlayers.remove(uuid);
    }

    private void generateTeam() {
        reloadConfig();
        if (board.getTeam("AFK") == null)
            afkTeam = board.registerNewTeam("AFK");
        if (getConfig().getBoolean("ShowInPlayerTag"))
            afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        else
            afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        if (getConfig().getBoolean("ShouldCollide"))
            afkTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);
        else
            afkTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        String prefix = getConfig().getString("Prefix", "&4AFK&r ");
        String suffix = getConfig().getString("Suffix", "");
        if (prefix != null && !prefix.equals(""))
            afkTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        if (suffix != null && !suffix.equals(""))
            afkTeam.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
    }

    @EventHandler
    public void onAFKStart(AFKStartEvent e) {
        enableAFK(e.getPlayer().getUUID());
    }

    @EventHandler
    public void onAFKStop(AFKStopEvent e) {
        disableAFK(e.getPlayer().getUUID());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.getPlayer().setScoreboard(board);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        disableAFK(e.getPlayer().getUniqueId());
    }
}
