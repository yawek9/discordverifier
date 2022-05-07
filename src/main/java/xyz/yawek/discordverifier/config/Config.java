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

package xyz.yawek.discordverifier.config;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.kyori.adventure.text.Component;

import java.util.LinkedHashMap;

public class Config {

    private final ConfigProvider configProvider;
    private final ConfigUtils configUtils;

    public Config(ConfigProvider configProvider) {
        this.configProvider = configProvider;

        this.configUtils = new ConfigUtils(configProvider);
    }

    public boolean useMySQL() {
        return configProvider.getString("data.database") != null
                && configProvider.getString("data.database").equalsIgnoreCase("mysql");
    }

    // Data

    public String databaseAddress() {
        return configProvider.getString("data.mysql.address");
    }

    public String databasePort() {
        return configProvider.getString("data.mysql.port");
    }

    public String databaseName() {
        return configProvider.getString("data.mysql.database-name");
    }

    public String databaseUser() {
        return configProvider.getString("data.mysql.user");
    }

    public String databasePassword() {
        return configProvider.getString("data.mysql.password");
    }

    // Discord

    public String discordToken() {
        return configProvider.getString("discord.token");
    }

    public String guildId() {
        return configProvider.getString("discord.guild-id");
    }

    public String channelId() {
        return configProvider.getString("discord.channel-id");
    }

    public int messageDeleteDelay() {
        return configProvider.getInt("discord.delete-message-after");
    }

    public int verificationExpireTime() {
        return configProvider.getInt("discord.verification-expire-time");
    }

    public boolean oneRoleLimit() {
        return configProvider.getBoolean("discord.one-role-limit");
    }

    public boolean forceNicknames() {
        return configProvider.getBoolean("discord.force-nicknames");
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, String> groupsRoles() {
        return (LinkedHashMap<String, String>) configProvider.getMap("discord.roles");
    }

    // Chat messages

    public Component noPermission() {
        return configUtils.prefixedMessage("messages.chat.no-permission");
    }

    public Component notFromConsole() {
        return configUtils.prefixedMessage("messages.chat.not-from-console");
    }

    public Component infoCommandUsage() {
        return configUtils.prefixedMessage("messages.chat.info-command-usage");
    }

    public Component adminCommandUsage() {
        return configUtils.listPrefixedMessage("messages.chat.admin-command-usage");
    }

    public Component playerNotVerified() {
        return configUtils.prefixedMessage("messages.chat.player-not-verified");
    }

    public Component playerNotFound() {
        return configUtils.prefixedMessage("messages.chat.player-not-found");
    }

    public Component playerInfo(String nickname, String uuid, String discordId,
                                String discordName, boolean online) {
        String onlineString = online
                ? configProvider.getString("messages.chat.online-in-message")
                : configProvider.getString("messages.chat.offline-in-message");
        return configUtils.listPrefixedMessage("messages.chat.player-info",
                nickname, uuid, discordId, discordName, onlineString);
    }

    public Component discordInfo() {
        return configUtils.prefixedMessage("messages.chat.discord-info");
    }

    public Component verificationRequest(String discordName) {
        return configUtils.prefixedMessage("messages.chat.verification-request", discordName);
    }

    public Component verificationExpired(String discordName) {
        return configUtils.prefixedMessage("messages.chat.verification-expired", discordName);
    }

    public Component verificationDenied() {
        return configUtils.prefixedMessage("messages.chat.verification-denied");
    }

    public Component verifiedSuccessfully(String discordName) {
        return configUtils.prefixedMessage("messages.chat.verified-successfully", discordName);
    }

    public Component noRequests() {
        return configUtils.prefixedMessage("messages.chat.no-requests");
    }

    public Component notVerified() {
        return configUtils.prefixedMessage("messages.chat.not-verified");
    }

    public Component verificationCanceled() {
        return configUtils.prefixedMessage("messages.chat.verification-canceled");
    }

    public Component notVerifiedYet() {
        return configUtils.prefixedMessage("messages.chat.not-verified-yet");
    }

    public Component configReloaded() {
        return configUtils.prefixedMessage("messages.chat.config-reloaded");
    }

    // Discord messages

    public MessageEmbed playerNotFound(String nickname) {
        return new EmbedBuilder()
                .setTitle(configUtils.stringWithArgs(
                        "messages.discord.player-not-found.title", nickname))
                .setDescription(configUtils.stringWithArgs(
                        "messages.discord.player-not-found.body", nickname))
                .setFooter(configUtils.stringWithArgs(
                        "messages.discord.player-not-found.footer", nickname))
                .build();
    }

    public MessageEmbed playerAlreadyVerified(String nickname) {
        return new EmbedBuilder()
                .setTitle(configUtils.stringWithArgs(
                        "messages.discord.player-already-verified.title", nickname))
                .setDescription(configUtils.stringWithArgs(
                        "messages.discord.player-already-verified.body", nickname))
                .setFooter(configUtils.stringWithArgs(
                        "messages.discord.player-already-verified.footer", nickname))
                .build();
    }

    public MessageEmbed discordAlreadyVerified(String nickname) {
        return new EmbedBuilder()
                .setTitle(configUtils.stringWithArgs(
                        "messages.discord.discord-already-verified.title", nickname))
                .setDescription(configUtils.stringWithArgs(
                        "messages.discord.discord-already-verified.body", nickname))
                .setFooter(configUtils.stringWithArgs(
                        "messages.discord.discord-already-verified.footer", nickname))
                .build();
    }

    public MessageEmbed verificationAccepted(String nickname) {
        return new EmbedBuilder()
                .setTitle(configUtils.stringWithArgs(
                        "messages.discord.verification-accepted.title", nickname))
                .setDescription(configUtils.stringWithArgs(
                        "messages.discord.verification-accepted.body", nickname))
                .setFooter(configUtils.stringWithArgs(
                        "messages.discord.verification-accepted.footer", nickname))
                .build();
    }

    public MessageEmbed verificationDenied(String nickname) {
        return new EmbedBuilder()
                .setTitle(configUtils.stringWithArgs(
                        "messages.discord.verification-denied.title", nickname))
                .setDescription(configUtils.stringWithArgs(
                        "messages.discord.verification-denied.body", nickname))
                .setFooter(configUtils.stringWithArgs(
                        "messages.discord.verification-denied.footer", nickname))
                .build();
    }

    public MessageEmbed verificationSuccess() {
        return new EmbedBuilder()
                .setTitle(configProvider.getString(
                        "messages.discord.verification-success.title"))
                .setDescription(configProvider.getString(
                        "messages.discord.verification-success.body"))
                .setFooter(configProvider.getString(
                        "messages.discord.verification-success.footer"))
                .build();
    }

}
