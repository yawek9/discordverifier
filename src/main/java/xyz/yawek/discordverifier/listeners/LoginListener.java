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
import xyz.yawek.discordverifier.data.DataManager;
import xyz.yawek.discordverifier.modules.VerificationManager;
import xyz.yawek.discordverifier.player.PlayerData;
import xyz.yawek.discordverifier.utils.VelocityMessageUtils;

public class LoginListener {

    @Subscribe
    public EventTask onPlayerLogin(LoginEvent e) {
        return EventTask.async(() -> {
            DataManager.createOrUpdatePlayerData(e.getPlayer().getUniqueId(), e.getPlayer().getUsername());

            VerificationManager.updateRoles(e.getPlayer());
            VerificationManager.updateNickname(e.getPlayer());

            PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());

            if (!playerData.isVerified())
                VelocityMessageUtils.sendMessageFromConfig(
                        e.getPlayer(),
                        "NotVerifiedYet",
                        true
                );
        });
    }

}
