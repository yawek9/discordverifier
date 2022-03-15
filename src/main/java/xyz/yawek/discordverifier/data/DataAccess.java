package xyz.yawek.discordverifier.data;

import java.util.UUID;

public abstract class DataAccess {

    public abstract void openDatabaseConnection();

    public abstract void closeDatabaseConnection();

    public abstract String getNickname(UUID uuid);

    public abstract void setNickname(UUID uuid, String nickname);

    public abstract UUID getUUID(String nickname);

    public abstract UUID getUUIDByDiscordId(String discordId);

    public abstract void setUUID(String nickname, UUID uuid);

    public abstract void setVerified(UUID uuid);

    public abstract void setUnVerified(UUID uuid);

    public abstract boolean isVerified(UUID uuid);

    public abstract String getDiscordId(UUID uuid);

    public abstract void setDiscordId(UUID uuid, String discordId);

    public abstract boolean isVerified(String memberId);

    public abstract void createOrUpdatePlayerData(UUID uuid, String nickname);

}
