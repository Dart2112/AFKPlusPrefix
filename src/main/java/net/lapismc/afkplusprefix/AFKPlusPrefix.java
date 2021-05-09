package net.lapismc.afkplusprefix;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.afkplus.util.core.LapisCoreConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.UUID;

public final class AFKPlusPrefix extends JavaPlugin implements Listener {

    private final ArrayList<UUID> afkPlayers = new ArrayList<>();
    LapisCoreConfiguration config;

    @Override
    public void onEnable() {
        config = ((AFKPlus) Bukkit.getPluginManager().getPlugin("AFKPlus")).config;
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        if (getConfig().getBoolean("CompatibilityMode")) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                for (UUID uuid : afkPlayers) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null)
                        continue;
                    if (p.getScoreboard().getTeam("AFK") == null) {
                        generateTeam(p.getScoreboard());
                    }
                    if (!p.getScoreboard().getTeam("AFK").hasEntry(p.getName())) {
                        p.getScoreboard().getTeam("AFK").addEntry(p.getName());
                    }
                }
            }, 5, 5);
        }
    }

    private void enableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        if (p.getScoreboard().getTeam("AFK") == null) {
            generateTeam(p.getScoreboard());
        }
        Team t = p.getScoreboard().getTeam("AFK");
        t.addEntry(p.getName());
        afkPlayers.add(uuid);
    }

    private void disableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        if (p.getScoreboard().getTeam("AFK") == null) {
            generateTeam(p.getScoreboard());
        }
        Team t = p.getScoreboard().getTeam("AFK");
        t.removeEntry(p.getName());
        afkPlayers.remove(uuid);
    }

    private void generateTeam(Scoreboard s) {
        Team afkTeam = s.registerNewTeam("AFK");
        reloadConfig();
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
            afkTeam.setPrefix(config.colorMessage(prefix));
        if (suffix != null && !suffix.equals(""))
            afkTeam.setSuffix(config.colorMessage(suffix));
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
    public void onPlayerQuit(PlayerQuitEvent e) {
        disableAFK(e.getPlayer().getUniqueId());
    }
}
