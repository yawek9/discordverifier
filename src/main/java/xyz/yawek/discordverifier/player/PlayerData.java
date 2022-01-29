package xyz.yawek.discordverifier.player;

import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.data.DataManager;
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

        lastNickname = DataManager.getNickname(uuid);
        verified = DataManager.isVerified(uuid);

        online = VelocityDiscordVerifier.getServer().getPlayer(lastNickname).isPresent();

        if (verified) {
            discordId = DataManager.getDiscordId(uuid);
            discordName = JDAManager.getDiscordName(discordId);
        }
    }

    public PlayerData(String lastNickname) {
        this.lastNickname = lastNickname;

        uuid = DataManager.getUUID(lastNickname);
        if (uuid != null) {
            verified = DataManager.isVerified(uuid);
        }

        online = VelocityDiscordVerifier.getServer().getPlayer(lastNickname).isPresent();

        if (verified) {
            discordId = DataManager.getDiscordId(uuid);
            discordName = JDAManager.getDiscordName(discordId);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;

        DataManager.setUUID(lastNickname, uuid);
    }

    public String getLastNickname() {
        return lastNickname;
    }

    public void setLastNickname(String lastNickname) {
        this.lastNickname = lastNickname;

        DataManager.setNickname(uuid, lastNickname);
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;

        if (verified) {
            DataManager.setVerified(uuid);
        } else {
            DataManager.setUnVerified(uuid);
        }
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;

        DataManager.setDiscordId(uuid, discordId);
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
