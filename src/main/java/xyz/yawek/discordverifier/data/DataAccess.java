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

import java.util.UUID;

public interface DataAccess {

    void openDatabaseConnection();

    void closeDatabaseConnection();

     String getNickname(UUID uuid);

     void setNickname(UUID uuid, String nickname);

     String getUUID(String nickname);

     String getUUIDByDiscordId(String discordId);

     void setUUID(String nickname, UUID uuid);

     boolean isVerified(UUID uuid);

     void setVerified(UUID uuid, boolean verified);

     String getDiscordId(UUID uuid);

     void setDiscordId(UUID uuid, String discordId);

     boolean isVerified(String memberId);

     void createOrUpdatePlayerData(UUID uuid, String nickname);

}
