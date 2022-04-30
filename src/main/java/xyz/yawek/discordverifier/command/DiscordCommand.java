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

package xyz.yawek.discordverifier.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.config.Config;
import xyz.yawek.discordverifier.manager.VerifiableUserManager;
import xyz.yawek.discordverifier.manager.VerificationManager;
import xyz.yawek.discordverifier.user.VerifiableUser;

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
        Config config = verifier.getConfig();
        VerifiableUserManager userManager = verifier.getUserManager();
        VerificationManager verification = verifier.getVerificationManager();

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            if (args.length < 1) {
                source.sendMessage(config.adminCommandUsage());
                return;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                verifier.reload();
                source.sendMessage(config.configReloaded());
                return;
            }
            if (args.length <= 1) {
                source.sendMessage(config.adminCommandUsage());
                return;
            }
            Optional<VerifiableUser> userOptional = userManager.retrieveByNickname(args[1]);
            if (userOptional.isEmpty()) {
                source.sendMessage(config.playerNotFound());
                return;
            }
            VerifiableUser user = userOptional.get();
            if (user.getDiscordId().isEmpty()) {
                source.sendMessage(config.playerNotVerified());
                return;
            }
            source.sendMessage(config.playerInfo(
                    user.getLastNickname().orElse(""),
                    user.getUUID().toString(),
                    user.getDiscordId().orElse(""),
                    user.getDiscordName().orElse(""),
                    user.isOnline()));
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
                            source.sendMessage(config.notVerified());
                            return;
                        }
                        verification.removeRoles((Player) source);
                        userManager.updateUser(user.toBuilder()
                                .discordId(null)
                                .discordName(null)
                                .verified(false)
                                .build());
                        source.sendMessage(config.verificationCanceled());
                        return;
                    }
                }
                source.sendMessage(config.adminCommandUsage());
                return;
            }
            Optional<VerifiableUser> userOptional = userManager.retrieveByNickname(args[1]);
            if (userOptional.isEmpty()) {
                source.sendMessage(config.playerNotFound());
                return;
            }
            VerifiableUser user = userOptional.get();
            if (user.getDiscordId().isEmpty()) {
                source.sendMessage(config.playerNotVerified());
                return;
            }
            source.sendMessage(config.playerInfo(
                    user.getLastNickname().orElse(""),
                    user.getUUID().toString(),
                    user.getDiscordId().orElse(""),
                    user.getDiscordName().orElse(""),
                    user.isOnline()));
            return;
        }
        if (args.length == 0) {
            source.sendMessage(config.discordInfo());
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
                source.sendMessage(config.notVerified());
                return;
            }
            verification.removeRoles((Player) source);
            userManager.updateUser(user.toBuilder()
                    .discordId(null)
                    .discordName(null)
                    .verified(false)
                    .build());
            source.sendMessage(config.verificationCanceled());
            return;
        }
        source.sendMessage(config.discordInfo());
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
