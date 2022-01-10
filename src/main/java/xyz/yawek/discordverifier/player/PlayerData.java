package xyz.yawek.discordverifier.player;

import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.data.MySQLDataAccess;
import xyz.yawek.discordverifier.modules.JDAManager;

import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private String lastNickname;
    private boolean verified;
    private String discordId;
    private String discordName;
    private boolean online;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;

        lastNickname = MySQLDataAccess.getNickname(uuid);
        verified = MySQLDataAccess.isVerified(uuid);

        online = VelocityDiscordVerifier.getServer().getPlayer(lastNickname).isPresent();

        if (verified) {
            discordId = MySQLDataAccess.getDiscordId(uuid);
            discordName = JDAManager.getDiscordName(discordId);
        }
    }

    public PlayerData(String lastNickname) {
        this.lastNickname = lastNickname;

        uuid = MySQLDataAccess.getUUID(lastNickname);
        if (uuid != null) {
            verified = MySQLDataAccess.isVerified(uuid);
        }

        online = VelocityDiscordVerifier.getServer().getPlayer(lastNickname).isPresent();

        if (verified) {
            discordId = MySQLDataAccess.getDiscordId(uuid);
            discordName = JDAManager.getDiscordName(discordId);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;

        MySQLDataAccess.setUUID(lastNickname, uuid);
    }

    public String getLastNickname() {
        return lastNickname;
    }

    public void setLastNickname(String lastNickname) {
        this.lastNickname = lastNickname;

        MySQLDataAccess.setNickname(uuid, lastNickname);
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;

        if (verified) {
            MySQLDataAccess.setVerified(uuid);
        } else {
            MySQLDataAccess.setUnVerified(uuid);
        }
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;

        MySQLDataAccess.setDiscordId(uuid, discordId);
    }

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

}
