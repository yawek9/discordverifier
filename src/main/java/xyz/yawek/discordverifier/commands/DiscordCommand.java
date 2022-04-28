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

package xyz.yawek.discordverifier.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.config.ConfigProvider;
import xyz.yawek.discordverifier.manager.VerifiableUserManager;
import xyz.yawek.discordverifier.manager.VerificationManager;
import xyz.yawek.discordverifier.user.VerifiableUser;
import xyz.yawek.discordverifier.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DiscordCommand implements SimpleCommand {

    private final DiscordVerifier verifier;

    public DiscordCommand(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public void execute(Invocation invocation) {
        ConfigProvider config = verifier.getConfigProvider();
        VerifiableUserManager userManager = verifier.getUserManager();
        VerificationManager verification = verifier.getVerificationManager();

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            if (args.length < 1) {
                MessageUtils.sendMessageFromConfig(
                        source, "AdminCommandUsage", true);
                return;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                verifier.reload();
                MessageUtils.sendMessageFromConfig(
                        source, "ConfigReloaded", true);
                return;
            }
            Optional<VerifiableUser> userOptional = userManager.retrieveByNickname(args[1]);
            if (userOptional.isEmpty()) {
                MessageUtils.sendMessageFromConfig(
                        source, "PlayerNotFound", true);
                return;
            }
            VerifiableUser user = userOptional.get();
            if (user.getDiscordId().isEmpty()) {
                MessageUtils.sendMessageFromConfig(
                        source, "PlayerNotVerified", true);
                return;
            }
            String isOnline;
            if (user.isOnline()) {
                isOnline = config.getString("Online");
            } else {
                isOnline = config.getString("Offline");
            }
            MessageUtils.sendMessageFromConfig(
                    source,
                    "PlayerInfo",
                    true,
                    user.getLastNickname().orElse(""),
                    user.getUUID().toString(),
                    user.getDiscordId().get(),
                    user.getDiscordName().orElse(""),
                    isOnline
            );
            return;
        }

        if (source.hasPermission("discordverifier.admin")) {
            if (args.length < 2) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("accept")) {
                        verification.completeVerification(
                                (Player) source, true);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("deny")) {
                        verification.completeVerification(
                                (Player) source, false);
                        return;
                    }
                    if (args[0].equalsIgnoreCase("unlink")) {
                        VerifiableUser user =
                                userManager.create(((Player) source).getUniqueId());
                        if (user.getDiscordId().isEmpty()) {
                            MessageUtils.sendMessageFromConfig(
                                    source, "NotVerified", true);
                            return;
                        }
                        verification.removeRoles((Player) source);
                        userManager.updateUser(user.toBuilder()
                                .discordId(null)
                                .discordName(null)
                                .verified(false)
                                .build());
                        MessageUtils.sendMessageFromConfig(
                                source, "UnverifiedSuccesfully", true);
                        return;
                    }
                }
                MessageUtils.sendMessageFromConfig(
                        source, "AdminCommandUsage", true);
                return;
            }
            Optional<VerifiableUser> userOptional = userManager.retrieveByNickname(args[1]);
            if (userOptional.isEmpty()) {
                MessageUtils.sendMessageFromConfig(
                        source, "PlayerNotFound", true);
                return;
            }
            VerifiableUser user = userOptional.get();
            if (user.getDiscordId().isEmpty()) {
                MessageUtils.sendMessageFromConfig(
                        source, "PlayerNotVerified", true);
                return;
            }
            String isOnline;
            if (user.isOnline()) {
                isOnline = config.getString("Online");
            } else {
                isOnline = config.getString("Offline");
            }
            MessageUtils.sendMessageFromConfig(
                    source,
                    "PlayerInfo",
                    true,
                    user.getLastNickname().orElse(""),
                    user.getUUID().toString(),
                    user.getDiscordId().get(),
                    user.getDiscordName().orElse(""),
                    isOnline
            );
            return;
        }
        if (args.length == 0) {
            MessageUtils.sendMessageFromConfig(source, "DiscordInfo", true);
            return;
        }
        if (args[0].equalsIgnoreCase("accept")) {
            verification.completeVerification((Player) source, true);
            return;
        }
        if (args[0].equalsIgnoreCase("deny")) {
            verification.completeVerification((Player) source, false);
            return;
        }
        if (args[0].equalsIgnoreCase("unlink")) {
            VerifiableUser user = userManager.create(((Player) source).getUniqueId());
            if (user.getDiscordId().isEmpty()) {
                MessageUtils.sendMessageFromConfig(source, "NotVerified", true);
                return;
            }
            verification.removeRoles((Player) source);
            userManager.updateUser(user.toBuilder()
                    .discordId(null)
                    .discordName(null)
                    .verified(false)
                    .build());
            MessageUtils.sendMessageFromConfig(source, "UnverifiedSuccesfully", true);
            return;
        }

        MessageUtils.sendMessageFromConfig(source, "DiscordInfo", true);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("discordverifier.discord");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        List<String> firstArguments = new ArrayList<>();

        firstArguments.add("accept");
        firstArguments.add("deny");
        firstArguments.add("unlink");

        if (source.hasPermission("discordverifier.admin")) {
            firstArguments.add("reload");
        }

        if (args.length == 1) {
            return CompletableFuture.completedFuture(firstArguments);
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

}
