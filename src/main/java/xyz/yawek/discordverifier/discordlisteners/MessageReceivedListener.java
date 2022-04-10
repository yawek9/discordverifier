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

package xyz.yawek.discordverifier.discordlisteners;

import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.data.DataManager;
import xyz.yawek.discordverifier.modules.JDAManager;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;
import xyz.yawek.discordverifier.modules.VerificationManager;
import xyz.yawek.discordverifier.player.PlayerData;

import java.util.concurrent.TimeUnit;

public class MessageReceivedListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (!e.getChannel().getId().equalsIgnoreCase(VelocityConfigManager.getString("VerificationChannelID"))) {
            return;
        }

        if (JDAManager.isOtherBot(e.getAuthor())) {
            return;
        }

        if (JDAManager.isBotItself(e.getAuthor())) {
            e.getMessage().delete().queueAfter(VelocityConfigManager.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }

        if (e.getMessage().getContentRaw().length() > 100) {
            e.getMessage().delete().queue();
            return;
        }

        if (!e.getMessage().getContentRaw().contains("!verify ")) {
            e.getMessage().delete().queue();
            return;
        }

        String nickname = e.getMessage().getContentRaw().replaceAll("!verify ", "");

        if (nickname.length() > 40) {
            e.getMessage().delete().queue();
            return;
        }

        if (VelocityDiscordVerifier.getServer().getPlayer(nickname).isEmpty()) {
            JDAManager.sendEmbedMessage(
                    e.getTextChannel(),
                    VelocityConfigManager.getString("PlayerNotFoundTitle").replaceAll("%NICKNAME%", nickname),
                    VelocityConfigManager.getString("PlayerNotFoundBody").replaceAll("%NICKNAME%", nickname),
                    VelocityConfigManager.getString("PlayerNotFoundFooter").replaceAll("%NICKNAME%", nickname)
            );

            e.getMessage().delete().queueAfter(VelocityConfigManager.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }

        PlayerData playerData = new PlayerData(nickname);

        if (playerData.isVerified()) {
            JDAManager.sendEmbedMessage(
                    e.getTextChannel(),
                    VelocityConfigManager.getString("PlayerAlreadyVerifiedTitle").replaceAll("%NICKNAME%", nickname),
                    VelocityConfigManager.getString("PlayerAlreadyVerifiedBody").replaceAll("%NICKNAME%", nickname),
                    VelocityConfigManager.getString("PlayerAlreadyVerifiedFooter").replaceAll("%NICKNAME%", nickname)
            );

            e.getMessage().delete().queueAfter(VelocityConfigManager.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }

        if (DataManager.isVerified(e.getMember().getId())) {
            JDAManager.sendEmbedMessage(
                    e.getTextChannel(),
                    VelocityConfigManager.getString("DiscordAlreadyVerifiedTitle"),
                    VelocityConfigManager.getString("DiscordAlreadyVerifiedBody"),
                    VelocityConfigManager.getString("DiscordAlreadyVerifiedFooter")
            );

            return;
        }

        Player player = VelocityDiscordVerifier.getServer().getPlayer(nickname).get();

        VerificationManager.setVerificationPlayer(player, e.getMember());

        VerificationManager.startVerificationProcess(e.getMember(), player);

        JDAManager.sendEmbedMessage(
                e.getTextChannel(),
                VelocityConfigManager.getString("VerificationAcceptedTitle").replaceAll("%NICKNAME%", nickname),
                VelocityConfigManager.getString("VerificationAcceptedBody").replaceAll("%NICKNAME%", nickname),
                VelocityConfigManager.getString("VerificationAcceptedFooter").replaceAll("%NICKNAME%", nickname)
        );

        e.getMessage().delete().queueAfter(VelocityConfigManager.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
    }
}
