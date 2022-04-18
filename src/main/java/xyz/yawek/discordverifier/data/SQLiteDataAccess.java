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

import xyz.yawek.discordverifier.VelocityDiscordVerifier;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SQLiteDataAccess extends DataAccess {

    private Connection connection;

    @Override
    public void openDatabaseConnection() {
        try {
            if (!VelocityDiscordVerifier.getDataDirectory().toFile().exists()) {
                VelocityDiscordVerifier.getDataDirectory().toFile().mkdirs();
            }

            File databaseFile = new File(VelocityDiscordVerifier.getDataDirectory().toString(), "data.db");

            if(!databaseFile.exists()) {
                try {
                    databaseFile.createNewFile();
                } catch (IOException e) {
                    VelocityDiscordVerifier.getLogger().error("Couldn't create SQLite database file!");
                    e.printStackTrace();
                }
            }

            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);

            if (connection != null) {
                VelocityDiscordVerifier.getLogger()
                        .info("Successfully connected to the SQLite database.");
            }

            String sql = """
                        CREATE TABLE IF NOT EXISTS players (
                          uuid NOT NULL,
                          last_nickname DEFAULT NULL,
                          verified DEFAULT 0,
                          discord_id DEFAULT NULL,
                          version DEFAULT "1.0.6",
                          PRIMARY KEY (uuid)
                        )""";

            Statement statement = connection.createStatement();

            statement.execute(sql);
        } catch (Exception e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't connect to SQLite database.");
            e.printStackTrace();
        }
    }

    @Override
    public void closeDatabaseConnection() {
        try {
            if (connection != null) {
                connection.close();

                VelocityDiscordVerifier.getLogger().info("SQLite connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickname(UUID uuid) {
        try {
            String sql = "SELECT last_nickname FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't get last_nickname for uuid " + uuid.toString() + ".");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setNickname(UUID uuid, String nickname) {
        try {
            String sql = "UPDATE players SET last_nickname = ? WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set last_nickname for the player with UUID "
                            + uuid + " and nickname " + nickname + ".");
            e.printStackTrace();
        }
    }

    @Override
    public UUID getUUID(String nickname) {
        try {
            String sql = "SELECT uuid FROM players WHERE last_nickname = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, nickname);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString(1));
            } else {
                return null;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't get UUID for nickname " + nickname + ".");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UUID getUUIDByDiscordId(String discordId) {
        try {
            String sql = "SELECT uuid FROM players WHERE discord_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, discordId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString(1));
            } else {
                return null;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't get UUID for Discord ID " + discordId + ".");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setUUID(String nickname, UUID uuid) {
        try {
            String sql = "UPDATE players SET uuid = ? WHERE last_nickname = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, nickname);

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set UUID for the player with UUID " + uuid + " and nickname " + nickname + ".");
            e.printStackTrace();
        }
    }

    @Override
    public void setVerified(UUID uuid) {
        try {
            String sql = "UPDATE players SET verified = true WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set verified for the player with UUID " + uuid + ".");
            e.printStackTrace();
        }
    }

    @Override
    public void setUnVerified(UUID uuid) {
        try {
            String sql = "UPDATE players SET verified = false WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set unverified for the player with UUID " + uuid + ".");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isVerified(UUID uuid) {
        try {
            String sql = "SELECT verified FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            } else {
                return false;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't check if is verified for the UUID " + uuid.toString() + ".");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getDiscordId(UUID uuid) {
        try {
            String sql = "SELECT discord_id FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't get Discord ID for UUID " + uuid.toString() + ".");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setDiscordId(UUID uuid, String discordId) {
        try {
            String sql = "UPDATE players SET discord_id = ? WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, discordId);
            preparedStatement.setString(2, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set Discord ID for the player with UUID " + uuid + ".");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isVerified(String memberId) {
        try {
            String sql = "SELECT verified FROM players WHERE discord_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, memberId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            } else {
                return false;
            }
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't check if is verified for the member with ID " + memberId + ".");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void createOrUpdatePlayerData(UUID uuid, String nickname) {
        if (!recordExists("players", "UUID", uuid.toString())) {
            try {
                String sql = "INSERT INTO players (uuid, last_nickname, version) VALUES (?, ?, ?)";

                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, nickname);
                preparedStatement.setString(3, VelocityDiscordVerifier.VERSION);

                preparedStatement.execute();
            } catch (SQLException e) {
                VelocityDiscordVerifier.getLogger().error("Couldn't create default data for the player with UUID "
                        + uuid + " and nickname " + nickname + ".");
                e.printStackTrace();
            }
        } else {
            setNickname(uuid, nickname);
        }
    }

    private boolean recordExists(String tableName, String recordName, String recordValue) {
        try {
            String query = "SELECT * FROM " + tableName + " WHERE " + recordName + " = \"" + recordValue + "\"";

            Statement statement;

            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(query);

            return resultSet.next();
        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

}
