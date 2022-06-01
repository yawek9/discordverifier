/*
 * This file is part of DiscordVerifier, licensed under GNU GPLv3 license.
 * Copyright (C) 2022 yawek9
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.yawek.discordverifier.data;

import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;

public class SQLiteDataAccess implements DataAccess {

    private final DiscordVerifier verifier;
    private Connection connection;

    public SQLiteDataAccess(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void openDatabaseConnection() {
        Path directory = verifier.getDataDirectory();

        try {
            if (!directory.toFile().exists()) {
                directory.toFile().mkdirs();
            }
            File databaseFile = new File(directory.toString(), "data.db");
            if (!databaseFile.exists()) {
                try {
                    databaseFile.createNewFile();
                } catch (IOException e) {
                    LogUtils.errorDataAccess("Unable to create SQLite database file.");
                    e.printStackTrace();
                }
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
            String sql = """
                        CREATE TABLE IF NOT EXISTS players (
                          uuid NOT NULL,
                          last_nickname DEFAULT NULL,
                          verified DEFAULT 0,
                          discord_id DEFAULT NULL,
                          version DEFAULT "1.0.7",
                          PRIMARY KEY (uuid)
                        )""";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
            LogUtils.infoDataAccess("Successfully connected to the SQLite database.");
        } catch (Exception e) {
            LogUtils.errorDataAccess("Unable to connect to the SQLite database.");
            e.printStackTrace();
        }
    }

    @Override
    public void closeDatabaseConnection() {
        try {
            if (connection == null) {
                return;
            }
            connection.close();
            LogUtils.infoDataAccess("SQLite connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickname(UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT last_nickname FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get last nickname for uuid {}.", uuid.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setNickname(UUID uuid, String nickname) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE players SET last_nickname = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to set last nickname for the player " +
                    "with UUID {} and nickname {}.", uuid.toString(), nickname);
            e.printStackTrace();
        }
    }

    @Override
    public String getUUID(String nickname) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT uuid FROM players WHERE last_nickname = ?")) {
            preparedStatement.setString(1, nickname);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get UUID for nickname {}.", nickname);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getUUIDByDiscordId(String discordId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT uuid FROM players WHERE discord_id = ?")) {
            preparedStatement.setString(1, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get UUID " +
                    "for Discord ID {}.", discordId);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setUUID(String nickname, UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE players SET uuid = ? WHERE last_nickname = ?")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, nickname);
            preparedStatement.execute();
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to set UUID for the player " +
                    "with UUID {} and nickname {}.", uuid.toString(), nickname);
            e.printStackTrace();
        }
    }

    @Override
    public boolean isVerified(UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT verified FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getBoolean(1);
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to check if is verified " +
                    "for the UUID {}.", uuid.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isVerified(String memberId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT verified FROM players WHERE discord_id = ?")) {
            preparedStatement.setString(1, memberId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getBoolean(1);
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to check if is verified for " +
                    "the member with ID {}.", memberId);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setVerified(UUID uuid, boolean verified) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE players SET verified = ? WHERE uuid = ?")) {
            preparedStatement.setBoolean(1, verified);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to set verified for " +
                    "the player with UUID {}.", uuid.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String getDiscordId(UUID uuid) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT discord_id FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get Discord ID for UUID {}.", uuid.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDiscordId(UUID uuid, String discordId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE players SET discord_id = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, discordId);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to set Discord ID for " +
                    "the player with UUID {}.", uuid.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void createOrUpdatePlayerData(UUID uuid, String nickname) {
        if (recordExists("players", "UUID", uuid.toString())) {
            setNickname(uuid, nickname);
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO players (uuid, last_nickname, version) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, nickname);
            preparedStatement.setString(3, DiscordVerifier.VERSION);
            preparedStatement.execute();
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to create default data for " +
                    "the player with UUID {} and nickname {}.", uuid.toString(), nickname);
            e.printStackTrace();
        }
    }

    private boolean recordExists(String tableName, String recordName, String recordValue) {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM " + tableName + " WHERE " + recordName + " = \"" + recordValue + "\"";
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
