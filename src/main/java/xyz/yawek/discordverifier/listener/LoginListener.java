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

package xyz.yawek.discordverifier.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.manager.VerificationManager;
import xyz.yawek.discordverifier.user.VerifiableUser;

public class LoginListener {

    private final DiscordVerifier verifier;

    public LoginListener(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public EventTask onPlayerLogin(LoginEvent e) {
        return EventTask.async(() -> {
            Player player = e.getPlayer();

            verifier.getDataProvider().updateUserIdentity(
                            player.getUniqueId(), player.getUsername());

            VerificationManager verificationManager = verifier.getVerificationManager();
            verificationManager.updateRoles(player);
            verificationManager.updateNickname(player);

            VerifiableUser user =
                    verifier.getUserManager().create(player.getUniqueId());
            if (!user.isVerified())
                player.sendMessage(verifier.getConfig().notVerifiedYet());
        });
    }

}
