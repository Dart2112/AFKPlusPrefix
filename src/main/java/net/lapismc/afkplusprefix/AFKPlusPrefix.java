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
    private ArrayList<UUID> afkPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        afkTeam = board.registerNewTeam("AFK");
        afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        afkTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")));
        if (getConfig().getBoolean("CompatibilityMode")) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (UUID uuid : afkPlayers) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p.getScoreboard().getTeam("AFK") != afkTeam) {
                        forcePlayerAFK(p);
                    }
                    afkTeam.addEntry(p.getName());
                }
            }, 5, 5);
        }
    }

    private void enableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (getConfig().getBoolean("CompatibilityMode") && p.getScoreboard().getTeam("AFK") != afkTeam) {
            forcePlayerAFK(p);
        }
        afkTeam.addEntry(p.getName());
        afkPlayers.add(uuid);
    }

    private void disableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        afkTeam.removeEntry(p.getName());
        afkPlayers.remove(uuid);
    }

    private void forcePlayerAFK(Player p) {
        afkTeam = p.getScoreboard().registerNewTeam("AFK");
        afkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        afkTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")));
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
