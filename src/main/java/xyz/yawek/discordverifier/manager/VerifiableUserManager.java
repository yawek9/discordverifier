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

package xyz.yawek.discordverifier.manager;

import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.data.DataProvider;
import xyz.yawek.discordverifier.user.VerifiableUser;

import java.util.Optional;
import java.util.UUID;

public class VerifiableUserManager {

    private final DiscordVerifier verifier;

    public VerifiableUserManager(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public VerifiableUser create(UUID uuid) {
        DataProvider data = verifier.getDataProvider();

        VerifiableUser.VerifiableUserBuilder builder = VerifiableUser.builder(uuid);
        builder.lastNickname(data.getNickname(uuid).orElse(null));
        if (data.isVerified(uuid)) {
            builder.verified(true);
            data.getDiscordId(uuid).ifPresent(id -> {
                builder.discordId(id);
                verifier.getDiscordManager()
                        .getDiscordName(id)
                        .ifPresent(builder::discordName);
            });
        }
        builder.online(verifier.getServer().getPlayer(uuid).isPresent());
        return builder.build();
    }

    public Optional<VerifiableUser> retrieveByNickname(String nickname) {
        Optional<UUID> uuidOptional = verifier.getDataProvider().getUUID(nickname);
        if (uuidOptional.isEmpty()) return Optional.empty();
        return Optional.of(create(uuidOptional.get()));
    }

    public Optional<VerifiableUser> retrieveByMemberId(String memberId) {
        Optional<UUID> uuidOptional =
                verifier.getDataProvider().getUUIDByDiscordId(memberId);
        if (uuidOptional.isEmpty()) return Optional.empty();
        return Optional.of(create(uuidOptional.get()));
    }

    public void updateUser(VerifiableUser user) {
        verifier.getDataProvider().updateUser(user);
    }

}
