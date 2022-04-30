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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.config.Config;
import xyz.yawek.discordverifier.user.VerifiableUser;
import xyz.yawek.discordverifier.util.LogUtils;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiscordManager {

    private final DiscordVerifier verifier;
    private JDA jda;
    private long GUILD_ID;
    
    public DiscordManager(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public boolean setup() {
        Config config = verifier.getConfig();

        if (config.discordToken()
                .equalsIgnoreCase("ENTER_YOUR_BOT_TOKEN_HERE")
                || config.guildId()
                .equalsIgnoreCase("ENTER_YOUR_GUILD_ID_HERE")
                || config.channelId()
                .equalsIgnoreCase("ENTER_YOUR_CHANNEL_ID_HERE")) {
            LogUtils.errorDiscord("You have not set up 'discord' settings in the config.yml " +
                    "correctly. Make sure that everything is fine and restart the server.");
            return false;
        }

        this.GUILD_ID = Long.parseLong(config.guildId());
        try {
            jda = JDABuilder.create(
                    config.discordToken(),
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGE_TYPING,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_BANS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_TYPING,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_VOICE_STATES
            ).build().awaitReady();
            return true;
        } catch (LoginException | InterruptedException e) {
            LogUtils.errorDiscord("Unable to connect to the Discord bot. " +
                    "Make sure you set 'discord' settings correctly in the config.yml.");
            e.printStackTrace();
        }
        return false;
    }

    public void shutdown() {
        if (jda == null) return;
        jda.shutdown();
    }

    public Optional<String> getDiscordName(String memberId) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) return Optional.empty();
        Optional<Member> memberOptional = getMemberById(memberId);
        return memberOptional.map(Member::getEffectiveName);
    }

    public boolean isBotItself(User user) {
        return jda.getSelfUser() == user;
    }

    public boolean isOtherBot(User user) {
        return user.isBot() && jda.getSelfUser() != user;
    }

    public void sendInVerification(MessageEmbed messageEmbed) {
        TextChannel verificationChannel =
                jda.getTextChannelById(verifier.getConfig().channelId());
        if (verificationChannel == null) return;
        verificationChannel.sendMessageEmbeds(messageEmbed).queue();
    }

    public void addEventListener(Object object) {
        jda.addEventListener(object);
    }

    public Optional<Role> getRole(String id) {
        Role role = jda.getRoleById(id);
        return role != null ? Optional.of(role) : Optional.empty();
    }

    public void addRole(Member member, Role role) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) return;
        guild.addRoleToMember(member, role).queue();
    }

    public void removeRole(Member member, Role role) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) return;
        guild.removeRoleFromMember(member, role).queue();
    }

    public Optional<Member> getMemberById(String memberId) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) return Optional.empty();
        Member member = guild.getMemberById(memberId);
        return member != null ? Optional.of(member) : Optional.empty();
    }

    public void setNickname(Member member, String nickname) {
        if (member == null) return;
        member.modifyNickname(nickname).queue();
    }

    public List<VerifiableUser> getPlayersWithRole(String roleId) {
        Guild guild = jda.getGuildById(GUILD_ID);
        if (guild == null) return Collections.emptyList();

        Optional<Role> roleOptional = getRole(roleId);
        if (roleOptional.isEmpty()) return Collections.emptyList();

        return guild.getMembersWithRoles(roleOptional.get()).stream()
                .map(member -> {
                    Optional<VerifiableUser> userOptional = verifier
                            .getUserManager().retrieveByMemberId(member.getId());
                    if (userOptional.isEmpty()) return null;
                    return userOptional.get();
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
}
