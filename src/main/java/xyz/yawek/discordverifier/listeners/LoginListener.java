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

package xyz.yawek.discordverifier.listeners;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.manager.VerificationManager;
import xyz.yawek.discordverifier.user.VerifiableUser;
import xyz.yawek.discordverifier.utils.MessageUtils;

public class LoginListener {

    private final DiscordVerifier verifier;

    public LoginListener(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public EventTask onPlayerLogin(LoginEvent e) {
        return EventTask.async(() -> {
            verifier.getDataProvider().updateUserIdentity(
                            e.getPlayer().getUniqueId(), e.getPlayer().getUsername());

            VerificationManager verificationManager = verifier.getVerificationManager();
            verificationManager.updateRoles(e.getPlayer());
            verificationManager.updateNickname(e.getPlayer());

            VerifiableUser user =
                    verifier.getUserManager().create(e.getPlayer().getUniqueId());
            if (!user.isVerified())
                MessageUtils.sendMessageFromConfig(
                        e.getPlayer(),
                        "NotVerifiedYet",
                        true);
        });
    }

}
