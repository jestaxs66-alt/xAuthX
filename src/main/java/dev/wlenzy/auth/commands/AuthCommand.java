package dev.wlenzy.auth.commands;

import dev.wlenzy.auth.XAuthX;
import dev.wlenzy.auth.auth.AuthManager;
import dev.wlenzy.auth.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AuthCommand implements CommandExecutor {

    private final AuthManager am = XAuthX.getInstance().getAuthManager();
    private final DatabaseManager db = XAuthX.getInstance().getDatabaseManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("xauthx.admin")) {
            sender.sendMessage(am.getMessage("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§b/xauthx reload");
            sender.sendMessage("§b/xauthx unregister <oyuncu>");
            sender.sendMessage("§b/xauthx forcelogin <oyuncu>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            XAuthX.getInstance().reloadPluginConfigs();
            sender.sendMessage(am.getMessage("messages.reload"));
            return true;
        }

        if (sub.equals("unregister")) {
            if (args.length < 2) {
                sender.sendMessage("§cKullanım: /xauthx unregister <oyuncu>");
                return true;
            }
            String target = args[1];
            db.unregister(target);
            Player p = Bukkit.getPlayer(target);
            if (p != null && p.isOnline()) {
                am.removePlayer(p);
                am.startAuthSequence(p);
            }
            sender.sendMessage(am.getMessage("messages.unregistered-success"));
            return true;
        }

        if (sub.equals("forcelogin")) {
            if (args.length < 2) {
                sender.sendMessage("§cKullanım: /xauthx forcelogin <oyuncu>");
                return true;
            }
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null || !p.isOnline()) {
                sender.sendMessage(am.getMessage("messages.player-not-found"));
                return true;
            }
            am.authenticate(p);
            p.sendMessage(am.getMessage("messages.login-success"));
            sender.sendMessage(am.getMessage("messages.forced-login"));
            return true;
        }

        return true;
    }
}