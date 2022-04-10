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

import xyz.yawek.discordverifier.modules.VelocityConfigManager;

import java.util.UUID;

public class DataManager {

    private static DataAccess dataAccess;

    public static void setupDataManager() {
        if (VelocityConfigManager.getBoolean("UseMySQL")) {
            dataAccess = new MySQLDataAccess();
        } else {
            dataAccess = new SQLiteDataAccess();
        }

        dataAccess.openDatabaseConnection();
    }

    public static void shutdownDataManager() {
        dataAccess.closeDatabaseConnection();
    }

    public static String getNickname(UUID uuid) {
        return dataAccess.getNickname(uuid);
    }

    public static void setNickname(UUID uuid, String nickname) {
        dataAccess.setNickname(uuid, nickname);
    }

    public static UUID getUUID(String nickname) {
        return dataAccess.getUUID(nickname);
    }

    public static UUID getUUIDByDiscordId(String discordId) {
        return dataAccess.getUUIDByDiscordId(discordId);
    }

    public static void setUUID(String nickname, UUID uuid) {
        dataAccess.setUUID(nickname, uuid);
    }

    public static void setVerified(UUID uuid) {
        dataAccess.setVerified(uuid);
    }

    public static void setUnVerified(UUID uuid) {
        dataAccess.setUnVerified(uuid);
    }

    public static boolean isVerified(UUID uuid) {
        return dataAccess.isVerified(uuid);
    }

    public static String getDiscordId(UUID uuid) {
        return dataAccess.getDiscordId(uuid);
    }

    public static void setDiscordId(UUID uuid, String discordId) {
        dataAccess.setDiscordId(uuid, discordId);
    }

    public static boolean isVerified(String memberId) {
        return dataAccess.isVerified(memberId);
    }

    public static void createOrUpdatePlayerData(UUID uuid, String nickname) {
        dataAccess.createOrUpdatePlayerData(uuid, nickname);
    }

}
