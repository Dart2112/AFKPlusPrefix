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

import java.util.UUID;

public final class AFKPlusPrefix extends JavaPlugin implements Listener {

    private Scoreboard board;
    private Team afkTeam;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        afkTeam = board.registerNewTeam("AFK");
        afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        afkTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")));
    }

    private void enableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p.getScoreboard() != board && p.getScoreboard().getTeam("AFK") != afkTeam) {
            afkTeam = p.getScoreboard().registerNewTeam("AFK");
            afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            afkTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")));
        }
        afkTeam.addEntry(p.getName());
    }

    private void disableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        afkTeam.removeEntry(p.getName());
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
