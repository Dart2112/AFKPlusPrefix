package net.lapismc.afkplusprefix;

import net.lapismc.afkplus.AFKPlus;
import net.lapismc.afkplus.api.AFKStartEvent;
import net.lapismc.afkplus.api.AFKStopEvent;
import net.lapismc.afkplus.util.core.LapisCoreConfiguration;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;

public final class AFKPlusPrefix extends JavaPlugin implements Listener {

    private final ArrayList<UUID> afkPlayers = new ArrayList<>();
    private Chat chat = null;
    LapisCoreConfiguration config;

    @Override
    public void onEnable() {
        config = AFKPlus.getInstance().config;
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        //Setup VaultAPI Chat access
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            //Throw error and shutdown
            getLogger().severe("Vault not present, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            getLogger().severe("No chat provider present, make sure you have a plugin that hooks into Vault for us to use, disabling plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        chat = rsp.getProvider();
        getLogger().info(getName() + " v." + getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        afkPlayers.forEach(this::forceDisableAFK);
    }

    private void enableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        if (!getConfig().getString("AFK.Prefix", "").isEmpty())
            chat.setPlayerPrefix(p, getMessage(p, "AFK.Prefix"));
        if (!getConfig().getString("AFK.Suffix", "").isEmpty())
            chat.setPlayerSuffix(p, getMessage(p, "AFK.Suffix"));
        afkPlayers.add(uuid);
    }

    private void disableAFK(UUID uuid) {
        forceDisableAFK(uuid);
        afkPlayers.remove(uuid);
    }

    public void forceDisableAFK(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null)
            return;
        if (!getConfig().getString("NotAFK.Prefix").isEmpty())
            chat.setPlayerPrefix(p, getMessage(p, "NotAFK.Prefix"));
        else
            chat.setPlayerPrefix(p, "");
        if (!getConfig().getString("NotAFK.Suffix").isEmpty())
            chat.setPlayerSuffix(p, getMessage(p, "NotAFK.Suffix"));
        else
            chat.setPlayerSuffix(p, "");
    }

    /**
     * Get a message from the config, color replace and placeholder replace the contents
     *
     * @param p   The player for placeholder replacement
     * @param key The key of the message in the config
     * @return a colored and PAPI replaced message
     */
    private String getMessage(OfflinePlayer p, String key) {
        String format = getConfig().getString(key);
        String papi = AFKPlus.getInstance().config.replacePlaceholders(format, p);
        String message = AFKPlus.getInstance().config.colorMessage(papi);
        return message;
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
