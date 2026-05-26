package dev.wlenzy.auth;

import dev.wlenzy.auth.auth.AuthManager;
import dev.wlenzy.auth.commands.AuthCommand;
import dev.wlenzy.auth.commands.PlayerCommands;
import dev.wlenzy.auth.database.DatabaseManager;
import dev.wlenzy.auth.listeners.PlayerListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class XAuthX extends JavaPlugin {

    private static XAuthX instance;
    private DatabaseManager databaseManager;
    private AuthManager authManager;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getConsoleSender().sendMessage("§b               _   _     _  _ ");
        getServer().getConsoleSender().sendMessage("§b              | | | |   | |/ /");
        getServer().getConsoleSender().sendMessage("§b  __  __  __ _| |_| |__ | ' / ");
        getServer().getConsoleSender().sendMessage("§b  \\ \\/ / / _` | __| '_ \\|  <  ");
        getServer().getConsoleSender().sendMessage("§b   >  < | (_| | |_| | | | . \\ ");
        getServer().getConsoleSender().sendMessage("§b  /_/\\_\\ \\__,_|\\__|_| |_|_|\\_\\");
        getServer().getConsoleSender().sendMessage("§a        xAuthX Başarıyla Aktif Edildi!");

        saveDefaultConfig();
        createMessagesConfig();

        databaseManager = new DatabaseManager();
        authManager = new AuthManager();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("login").setExecutor(new PlayerCommands());
        getCommand("register").setExecutor(new PlayerCommands());
        getCommand("changepassword").setExecutor(new PlayerCommands());
        getCommand("xauthx").setExecutor(new AuthCommand());
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnection();
    }

    public static XAuthX getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void reloadPluginConfigs() {
        reloadConfig();
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        authManager.clearCache();
    }

    private void createMessagesConfig() {
        messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}