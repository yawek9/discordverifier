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
