package xyz.yawek.discordverifier.data;

import com.zaxxer.hikari.HikariDataSource;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;

import java.sql.*;
import java.util.UUID;

public class MySQLDataAccess extends DataAccess {

    private HikariDataSource hikari;

    @Override
    public void openDatabaseConnection() {
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", VelocityConfigManager.getString("DatabaseAddress"));
        hikari.addDataSourceProperty("port", VelocityConfigManager.getString("DatabasePort"));
        hikari.addDataSourceProperty("databaseName", VelocityConfigManager.getString("DatabaseName"));
        hikari.addDataSourceProperty("user", VelocityConfigManager.getString("DatabaseUser"));
        hikari.addDataSourceProperty("password", VelocityConfigManager.getString("DatabasePassword"));
        hikari.setPoolName("discordverifier-hikari");

        try (Connection connection = hikari.getConnection()) {
            if (connection != null) {
                VelocityDiscordVerifier.getLogger()
                        .info("Successfully connected to the MySQL database.");
            }

            String sql = """
                        CREATE TABLE IF NOT EXISTS players (
                          uuid varchar(100) NOT NULL,
                          last_nickname varchar(100) DEFAULT NULL,
                          verified tinyint(1) DEFAULT 0,
                          discord_id varchar(100) DEFAULT NULL,
                          version varchar(20) DEFAULT "1.0.3",
                          PRIMARY KEY (UUID)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;""";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.execute(sql);
        } catch (Exception e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't connect to MySQL database.");
            e.printStackTrace();
        }
    }

    @Override
    public void closeDatabaseConnection() {
        hikari.close();
        VelocityDiscordVerifier.getLogger()
                .info("Closed MySQL connection.");
    }

    @Override
    public String getNickname(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT last_nickname FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

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
        try (Connection connection = hikari.getConnection()) {
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT uuid FROM players WHERE last_nickname = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, nickname);

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

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
    public void setUUID(String nickname, UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
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
        try (Connection connection = hikari.getConnection()) {
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
        try (Connection connection = hikari.getConnection()) {
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
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT verified FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

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
    public boolean isVerified(String memberId) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT verified FROM players WHERE discord_id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, memberId);

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

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
    public String getDiscordId(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT discord_id FROM players WHERE uuid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();

            ResultSet resultSet = preparedStatement.getResultSet();

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
        try (Connection connection = hikari.getConnection()) {
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
    public void createOrUpdatePlayerData(UUID uuid, String nickname) {
        if (!recordExists("players", "UUID", uuid.toString())) {
            try (Connection connection = hikari.getConnection()) {
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
