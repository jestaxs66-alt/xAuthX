package dev.wlenzy.auth.auth;

import dev.wlenzy.auth.XAuthX;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class AuthManager {

    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();
    private final Map<UUID, BukkitTask> messageTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();
    private final List<Long> joinTimestamps = new ArrayList<>();

    public boolean isBotLimitTriggered() {
        if (!XAuthX.getInstance().getConfig().getBoolean("settings.antibot.enabled")) return false;
        long now = System.currentTimeMillis();
        joinTimestamps.removeIf(timestamp -> now - timestamp > 1000);
        joinTimestamps.add(now);
        return joinTimestamps.size() > XAuthX.getInstance().getConfig().getInt("settings.antibot.max-joins-per-second");
    }

    public boolean isAuthenticated(Player player) {
        return authenticatedPlayers.contains(player.getUniqueId());
    }

    public void authenticate(Player player) {
        UUID uuid = player.getUniqueId();
        authenticatedPlayers.add(uuid);
        failedAttempts.remove(uuid);
        cancelTasks(uuid);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        authenticatedPlayers.remove(uuid);
        failedAttempts.remove(uuid);
        cancelTasks(uuid);
    }

    public void incrementFailedAttempts(Player player) {
        UUID uuid = player.getUniqueId();
        int attempts = failedAttempts.getOrDefault(uuid, 0) + 1;
        failedAttempts.put(uuid, attempts);

        if (attempts >= XAuthX.getInstance().getConfig().getInt("settings.max-failed-attempts")) {
            player.kickPlayer(getMessage("messages.max-attempts"));
        }
    }

    public void startAuthSequence(Player player) {
        UUID uuid = player.getUniqueId();
        boolean isReg = XAuthX.getInstance().getDatabaseManager().isRegistered(player.getName());
        String msgKey = isReg ? "messages.login-prompt" : "messages.register-prompt";

        BukkitTask msgTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.sendMessage(getMessage(msgKey));
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(XAuthX.getInstance(), 0L, 60L);
        messageTasks.put(uuid, msgTask);

        BukkitTask timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.kickPlayer(getMessage("messages.timeout"));
                }
            }
        }.runTaskLater(XAuthX.getInstance(), XAuthX.getInstance().getConfig().getInt("settings.login-timeout-seconds") * 20L);
        timeoutTasks.put(uuid, timeoutTask);
    }

    public String getMessage(String path) {
        FileConfiguration msgConfig = XAuthX.getInstance().getMessagesConfig();
        String prefix = msgConfig.getString("prefix", "");
        String message = msgConfig.getString(path, "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public void clearCache() {
        messageTasks.values().forEach(BukkitTask::cancel);
        timeoutTasks.values().forEach(BukkitTask::cancel);
        messageTasks.clear();
        timeoutTasks.clear();
    }

    private void cancelTasks(UUID uuid) {
        if (messageTasks.containsKey(uuid)) {
            messageTasks.remove(uuid).cancel();
        }
        if (timeoutTasks.containsKey(uuid)) {
            timeoutTasks.remove(uuid).cancel();
        }
    }
}