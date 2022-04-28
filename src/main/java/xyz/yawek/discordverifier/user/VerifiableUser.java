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

package xyz.yawek.discordverifier.user;

import java.util.Optional;
import java.util.UUID;

public class VerifiableUser {

    private final UUID uuid;
    private final String lastNickname;
    private String discordId;
    private String discordName;
    private boolean verified;
    private final boolean online;

    public VerifiableUser(VerifiableUserBuilder verifiableUserBuilder) {
        this.uuid = verifiableUserBuilder.uuid;
        this.lastNickname = verifiableUserBuilder.lastNickname;
        this.discordId = verifiableUserBuilder.discordId;
        this.discordName = verifiableUserBuilder.discordName;
        this.verified = verifiableUserBuilder.verified;
        this.online = verifiableUserBuilder.online;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Optional<String> getLastNickname() {
        if (lastNickname == null) return Optional.empty();
        return Optional.of(lastNickname);
    }

    public Optional<String> getDiscordId() {
        if (discordId == null) return Optional.empty();
        return Optional.of(discordId);
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public Optional<String> getDiscordName() {
        if (discordName == null) return Optional.empty();
        return Optional.of(discordName);
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isOnline() {
        return online;
    }

    public VerifiableUserBuilder toBuilder() {
        return new VerifiableUserBuilder(uuid)
                .lastNickname(lastNickname)
                .discordId(discordId)
                .discordName(discordName)
                .verified(verified)
                .online(online);
    }

    public static VerifiableUserBuilder builder(UUID uuid) {
        return new VerifiableUserBuilder(uuid);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class VerifiableUserBuilder {

        private final UUID uuid;
        private String lastNickname = null;
        private String discordId = null;
        private String discordName = null;
        private boolean verified = false;
        private boolean online = false;

        public VerifiableUserBuilder(UUID uuid) {
            this.uuid = uuid;
        }

        public VerifiableUserBuilder lastNickname(String lastNickname) {
            this.lastNickname = lastNickname;
            return this;
        }

        public VerifiableUserBuilder discordId(String discordId) {
            this.discordId = discordId;
            return this;
        }

        public VerifiableUserBuilder discordName(String discordName) {
            this.discordName = discordName;
            return this;
        }

        public VerifiableUserBuilder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public VerifiableUserBuilder online(boolean online) {
            this.online = online;
            return this;
        }

        public VerifiableUser build() {
            return new VerifiableUser(this);
        }

    }

}
