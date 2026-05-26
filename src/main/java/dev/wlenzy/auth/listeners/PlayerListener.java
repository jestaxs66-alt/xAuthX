package dev.wlenzy.auth.listeners;

import dev.wlenzy.auth.XAuthX;
import dev.wlenzy.auth.auth.AuthManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class PlayerListener implements Listener {

    private final AuthManager authManager = XAuthX.getInstance().getAuthManager();

    public PlayerListener() {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        rootLogger.addFilter(new AbstractFilter() {
            @Override
            public Result filter(LogEvent event) {
                if (event.getMessage() != null) {
                    String message = event.getMessage().getFormattedMessage().toLowerCase();
                    if (message.contains("issued server command:") && 
                        (message.contains("/login") || message.contains("/register") || message.contains("/changepassword"))) {
                        return Result.DENY;
                    }
                }
                return Result.NEUTRAL;
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (authManager.isBotLimitTriggered()) {
            String kickMsg = ChatColor.translateAlternateColorCodes('&', 
                    XAuthX.getInstance().getConfig().getString("settings.antibot.kick-message"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMsg);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        authManager.startAuthSequence(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        authManager.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer())) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage().toLowerCase();
        
        if (msg.startsWith("/login") || msg.startsWith("/register") || msg.startsWith("/changepassword")) {
            event.setCancelled(true);
            
            String[] args = event.getMessage().split(" ");
            String cmd = args[0].substring(1);
            String[] cmdArgs = new String[args.length - 1];
            System.arraycopy(args, 1, cmdArgs, 0, cmdArgs.length);
            
            Bukkit.getPluginCommand(cmd).execute(player, cmd, cmdArgs);
        }

        if (!authManager.isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!authManager.isAuthenticated(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!authManager.isAuthenticated(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!authManager.isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}