package xyz.yawek.discordverifier.data;

import com.zaxxer.hikari.HikariDataSource;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;

import java.sql.*;
import java.util.UUID;

public class MySQLDataAccess {

    private static HikariDataSource hikari;

    public static void openDatabaseConnection() {
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
                        CREATE TABLE IF NOT EXISTS verify (
                          UUID varchar(100) NOT NULL,
                          PlayerName varchar(100) DEFAULT NULL,
                          Verified tinyint(1) DEFAULT 0,
                          DiscordID varchar(100) DEFAULT NULL,
                          Version varchar(20) DEFAULT "1.0.1",
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

    public static void closeDatabaseConnection() {
        hikari.close();
        VelocityDiscordVerifier.getLogger()
                .info("Closed MySQL connection.");
    }

    public static String getNickname(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT PlayerName FROM verify WHERE UUID = ?";

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
                    .error("Couldn't get nickname for UUID " + uuid.toString() + ".");
            e.printStackTrace();
            return null;
        }
    }

    public static void setNickname(UUID uuid, String nickname) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE verify SET PlayerName = ? WHERE UUID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set nickname for the player with UUID "
                            + uuid + " and nickname " + nickname + ".");
            e.printStackTrace();
        }
    }

    public static UUID getUUID(String nickname) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT UUID FROM verify WHERE PlayerName = ?";

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

    public static void setUUID(String nickname, UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE verify SET UUID = ? WHERE PlayerName = ?";

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

    public static void setVerified(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE verify SET Verified = true WHERE UUID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set verified for the player with UUID " + uuid + ".");
            e.printStackTrace();
        }
    }

    public static void setUnVerified(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE verify SET Verified = false WHERE UUID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());

            preparedStatement.execute();
        } catch (SQLException e) {
            VelocityDiscordVerifier.getLogger()
                    .error("Couldn't set unverified for the player with UUID " + uuid + ".");
            e.printStackTrace();
        }
    }

    public static boolean isVerified(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT Verified FROM verify WHERE UUID = ?";

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

    public static String getDiscordId(UUID uuid) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT DiscordID FROM verify WHERE UUID = ?";

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

    public static void setDiscordId(UUID uuid, String discordId) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "UPDATE verify SET DiscordID = ? WHERE UUID = ?";

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

    public static boolean isVerified(String memberId) {
        try (Connection connection = hikari.getConnection()) {
            String sql = "SELECT Verified FROM verify WHERE DiscordID = ?";

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

    public static void createOrUpdatePlayerData(UUID uuid, String nickname) {
        if (!recordExists("verify", "UUID", uuid.toString())) {
            try (Connection connection = hikari.getConnection()) {
                String sql = "INSERT INTO verify (UUID, PlayerName, Version) VALUES (?, ?, ?)";

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

    private static boolean recordExists(String tableName, String recordName, String recordValue) {
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
