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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.manager.DiscordManager;
import xyz.yawek.discordverifier.manager.VerificationManager;
import xyz.yawek.discordverifier.config.ConfigProvider;
import xyz.yawek.discordverifier.user.VerifiableUser;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MessageReceivedListener extends ListenerAdapter {

    private final DiscordVerifier verifier;

    public MessageReceivedListener(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        DiscordManager discord = verifier.getDiscordManager();
        ConfigProvider config = verifier.getConfigProvider();
        VerificationManager verification = verifier.getVerificationManager();

        if (!e.getChannel().getId().equalsIgnoreCase(config.getString("VerificationChannelID"))) {
            return;
        }
        if (discord.isOtherBot(e.getAuthor())) {
            return;
        }

        Message message = e.getMessage();
        if (discord.isBotItself(e.getAuthor())) {
            message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }
        if (e.getMessage().getContentRaw().length() > 100) {
            message.delete().queue();
            return;
        }
        if (!message.getContentRaw().contains("!verify ")) {
            message.delete().queue();
            return;
        }
        String nickname = message.getContentRaw().replaceAll("!verify ", "");
        if (nickname.length() > 40) {
            message.delete().queue();
            return;
        }
        Optional<VerifiableUser> user =
                verifier.getUserManager().retrieveByNickname(nickname);
        if (verifier.getServer().getPlayer(nickname).isEmpty() || user.isEmpty()) {
            discord.sendEmbed(e.getTextChannel(),
                    config.getString("PlayerNotFoundTitle").replaceAll("%NICKNAME%", nickname),
                    config.getString("PlayerNotFoundBody").replaceAll("%NICKNAME%", nickname),
                    config.getString("PlayerNotFoundFooter").replaceAll("%NICKNAME%", nickname));
            message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }
        if (user.get().isVerified()) {
            discord.sendEmbed(e.getTextChannel(),
                    config.getString("PlayerAlreadyVerifiedTitle").replaceAll("%NICKNAME%", nickname),
                    config.getString("PlayerAlreadyVerifiedBody").replaceAll("%NICKNAME%", nickname),
                    config.getString("PlayerAlreadyVerifiedFooter").replaceAll("%NICKNAME%", nickname));
            message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }
        Member member = e.getMember();
        if (member == null) {
            message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }
        Optional<VerifiableUser> discordUser =
                verifier.getUserManager().retrieveByMemberId(e.getMember().getId());
        if (discordUser.isPresent() && discordUser.get().isVerified()) {
            discord.sendEmbed(e.getTextChannel(),
                    config.getString("DiscordAlreadyVerifiedTitle"),
                    config.getString("DiscordAlreadyVerifiedBody"),
                    config.getString("DiscordAlreadyVerifiedFooter"));
            message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
            return;
        }

        Player player = verifier.getServer().getPlayer(nickname).get();
        verification.startVerification(e.getMember(), player);

        discord.sendEmbed(e.getTextChannel(),
                config.getString("VerificationAcceptedTitle").replaceAll("%NICKNAME%", nickname),
                config.getString("VerificationAcceptedBody").replaceAll("%NICKNAME%", nickname),
                config.getString("VerificationAcceptedFooter").replaceAll("%NICKNAME%", nickname));
        message.delete().queueAfter(config.getInt("DeleteMessageAfter"), TimeUnit.SECONDS);
    }

}
