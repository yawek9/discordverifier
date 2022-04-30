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

import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.user.VerifiableUser;

import java.util.Optional;
import java.util.UUID;

public class DataProvider {

    private final DiscordVerifier verifier;
    private DataAccess dataAccess;

    public DataProvider(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public void setup() {
        if (dataAccess != null) dataAccess.closeDatabaseConnection();
        dataAccess = verifier.getConfig().useMySQL()
                ? new MySQLDataAccess(verifier) : new SQLiteDataAccess(verifier);
        dataAccess.openDatabaseConnection();
    }

    public void shutdown() {
        dataAccess.closeDatabaseConnection();
    }

    public Optional<String> getNickname(UUID uuid) {
        String nickname = dataAccess.getNickname(uuid);
        if (nickname == null) return Optional.empty();
        return Optional.of(nickname);
    }

    public Optional<UUID> getUUID(String nickname) {
        String uuidString = dataAccess.getUUID(nickname);
        if (uuidString == null) return Optional.empty();
        return Optional.of(UUID.fromString(uuidString));
    }

    public Optional<UUID> getUUIDByDiscordId(String discordId) {
        String uuidString = dataAccess.getUUIDByDiscordId(discordId);
        if (uuidString == null) return Optional.empty();
        return Optional.of(UUID.fromString(uuidString));
    }

    public boolean isVerified(UUID uuid) {
        return dataAccess.isVerified(uuid);
    }

    public Optional<String> getDiscordId(UUID uuid) {
        String discordId = dataAccess.getDiscordId(uuid);
        if (discordId == null) return Optional.empty();
        return Optional.of(discordId);
    }

    public void updateUserIdentity(UUID uuid, String nickname) {
        dataAccess.createOrUpdatePlayerData(uuid, nickname);
    }

    public void updateUser(VerifiableUser user) {
        UUID uuid = user.getUUID();
        user.getLastNickname().ifPresent(s -> dataAccess.setNickname(uuid, s));
        dataAccess.setVerified(uuid, user.isVerified());
        user.getDiscordId().ifPresent(s -> dataAccess.setDiscordId(uuid, s));
        dataAccess.setVerified(uuid, user.isVerified());
    }

}
