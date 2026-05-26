package dev.wlenzy.auth.commands;

import dev.wlenzy.auth.XAuthX;
import dev.wlenzy.auth.auth.AuthManager;
import dev.wlenzy.auth.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommands implements CommandExecutor {

    private final AuthManager am = XAuthX.getInstance().getAuthManager();
    private final DatabaseManager db = XAuthX.getInstance().getDatabaseManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmdName = cmd.getName().toLowerCase();

        if (cmdName.equals("register")) {
            if (am.isAuthenticated(player)) {
                player.sendMessage(am.getMessage("messages.already-logged-in"));
                return true;
            }
            if (db.isRegistered(player.getName())) {
                player.sendMessage(am.getMessage("messages.already-registered"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(am.getMessage("messages.register-prompt"));
                return true;
            }
            if (!args[0].equals(args[1])) {
                player.sendMessage(am.getMessage("messages.password-mismatch"));
                return true;
            }
            String ip = player.getAddress().getAddress().getHostAddress();
            if (db.getIpCount(ip) >= XAuthX.getInstance().getConfig().getInt("settings.max-accounts-per-ip")) {
                player.sendMessage(am.getMessage("messages.max-ip-accounts"));
                return true;
            }

            db.register(player.getName(), args[0], ip);
            am.authenticate(player);
            player.sendMessage(am.getMessage("messages.register-success"));
            return true;
        }

        if (cmdName.equals("login")) {
            if (am.isAuthenticated(player)) {
                player.sendMessage(am.getMessage("messages.already-logged-in"));
                return true;
            }
            if (!db.isRegistered(player.getName())) {
                player.sendMessage(am.getMessage("messages.not-registered"));
                return true;
            }
            if (args.length < 1) {
                player.sendMessage(am.getMessage("messages.login-prompt"));
                return true;
            }

            if (db.checkPassword(player.getName(), args[0])) {
                am.authenticate(player);
                player.sendMessage(am.getMessage("messages.login-success"));
            } else {
                am.incrementFailedAttempts(player);
                if (player.isOnline()) {
                    player.sendMessage(am.getMessage("messages.wrong-password"));
                }
            }
            return true;
        }

        if (cmdName.equals("changepassword")) {
            if (!am.isAuthenticated(player)) {
                player.sendMessage(am.getMessage("messages.login-prompt"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cKullanım: /changepassword <eski> <yeni>");
                return true;
            }
            if (db.checkPassword(player.getName(), args[0])) {
                db.changePassword(player.getName(), args[1]);
                player.sendMessage(am.getMessage("messages.password-changed"));
            } else {
                player.sendMessage(am.getMessage("messages.wrong-password"));
            }
            return true;
        }

        return true;
    }
}