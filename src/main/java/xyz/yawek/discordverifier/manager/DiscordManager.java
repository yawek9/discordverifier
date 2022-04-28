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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.user.VerifiableUser;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DiscordManager {

    private final DiscordVerifier verifier;
    private JDA jda;
    private final long GUILD_ID;
    
    public DiscordManager(DiscordVerifier verifier) {
        this.verifier = verifier;
        this.GUILD_ID = Long.parseLong(verifier.getConfigProvider().getString("GuildID"));
        try {
            jda = JDABuilder.create(
                    verifier.getConfigProvider().getString("BotToken"),
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
            ).build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
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

    public void sendEmbed(TextChannel textChannel, String title, String body, String footer) {
        textChannel.sendMessageEmbeds(
                new EmbedBuilder().setTitle(title)
                        .setDescription(body)
                        .setFooter(footer)
                        .build()).queue();
    }

    public void sendVerificationEmbed(String title, String body, String footer) {
        TextChannel verificationChannel = jda.getTextChannelById(
                verifier.getConfigProvider().getString("VerificationChannelID"));
        if (verificationChannel == null) return;
        sendEmbed(verificationChannel, title, body, footer);
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

        List<VerifiableUser> users = new ArrayList<>();
        List<Member> members = guild.getMembersWithRoles(roleOptional.get());
        for (Member member : members) {
            Optional<VerifiableUser> userOptional =
                    verifier.getUserManager().retrieveByMemberId(member.getId());
            if (userOptional.isEmpty()) continue;
            users.add(userOptional.get());
        }
        return users;
    }
    
}
