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

import com.zaxxer.hikari.HikariDataSource;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.config.ConfigProvider;
import xyz.yawek.discordverifier.utils.LogUtils;

import java.sql.*;
import java.util.UUID;

public class MySQLDataAccess implements DataAccess {

    private final DiscordVerifier verifier;
    private HikariDataSource hikari;

    public MySQLDataAccess(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void openDatabaseConnection() {
        ConfigProvider config = verifier.getConfigProvider();
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        hikari.addDataSourceProperty("serverName",
                config.getString("DatabaseAddress"));
        hikari.addDataSourceProperty("port",
                config.getString("DatabasePort"));
        hikari.addDataSourceProperty("databaseName",
                config.getString("DatabaseName"));
        hikari.addDataSourceProperty("user",
                config.getString("DatabaseUser"));
        hikari.addDataSourceProperty("password",
                config.getString("DatabasePassword"));
        hikari.setPoolName("discordverifier-hikari");

        try (Connection connection = hikari.getConnection()) {
            String sql = """
                        CREATE TABLE IF NOT EXISTS players (
                          uuid varchar(100) NOT NULL,
                          last_nickname varchar(100) DEFAULT NULL,
                          verified tinyint(1) DEFAULT 0,
                          discord_id varchar(100) DEFAULT NULL,
                          version varchar(20) DEFAULT "1.0.6",
                          PRIMARY KEY (uuid)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;""";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute(sql);
            LogUtils.infoDataAccess("Successfully connected to the MySQL database.");
        } catch (Exception e) {
            LogUtils.errorDataAccess("Unable to connect to MySQL database.");
            e.printStackTrace();
        }
    }

    @Override
    public void closeDatabaseConnection() {
        hikari.close();
        LogUtils.infoDataAccess("Closed MySQL connection.");
    }

    @Override
    public String getNickname(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT last_nickname FROM players WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get last nickname for uuid {}.", uuid.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setNickname(UUID uuid, String nickname) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE players SET last_nickname = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT uuid FROM players WHERE last_nickname = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, nickname);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get UUID for nickname {}.", nickname);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getUUIDByDiscordId(String discordId) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT uuid FROM players WHERE discord_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, discordId);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE players SET uuid = ? WHERE last_nickname = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT verified FROM players WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT verified FROM players WHERE discord_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, memberId);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE players SET verified = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT discord_id FROM players WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            LogUtils.errorDataAccess("Unable to get Discord ID for UUID {}.", uuid.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDiscordId(UUID uuid, String discordId) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE players SET discord_id = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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

        try (Connection connection = hikari.getConnection()) {
            String sql = "INSERT INTO players (uuid, last_nickname, version) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        try (Connection connection = hikari.getConnection()) {
            String query = "SELECT * FROM " + tableName + " WHERE " + recordName + " = \"" + recordValue + "\"";
            Statement statement;
            statement = connection.createStatement();
            statement.execute(query);
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
