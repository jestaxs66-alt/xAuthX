package dev.wlenzy.auth.database;

import dev.wlenzy.auth.XAuthX;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseManager {

    private Connection connection;

    public DatabaseManager() {
        try {
            File dataFolder = XAuthX.getInstance().getDataFolder();
            if (!dataFolder.exists()) dataFolder.mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "auth.db").getAbsolutePath());
            
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT, ip TEXT)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRegistered(String username) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            ps.setString(1, username.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register(String username, String password, String ip) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO users (username, password, ip) VALUES (?, ?, ?)")) {
            ps.setString(1, username.toLowerCase());
            ps.setString(2, hashSHA256(password));
            ps.setString(3, ip);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkPassword(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            ps.setString(1, username.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password").equals(hashSHA256(password));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void changePassword(String username, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET password = ? WHERE username = ?")) {
            ps.setString(1, hashSHA256(newPassword));
            ps.setString(2, username.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unregister(String username) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM users WHERE username = ?")) {
            ps.setString(1, username.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getIpCount(String ip) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE ip = ?")) {
            ps.setString(1, ip);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String hashSHA256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}