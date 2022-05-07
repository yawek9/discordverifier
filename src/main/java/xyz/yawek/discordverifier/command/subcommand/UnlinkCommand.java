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

package xyz.yawek.discordverifier.command.subcommand;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.command.PermissibleCommand;
import xyz.yawek.discordverifier.config.Config;
import xyz.yawek.discordverifier.manager.VerifiableUserManager;
import xyz.yawek.discordverifier.user.VerifiableUser;

import java.util.Collections;
import java.util.List;

public class UnlinkCommand extends PermissibleCommand {

    public UnlinkCommand(DiscordVerifier verifier) {
        super(verifier, "discordverifier.unlink");
    }

    @Override
    protected void handle(CommandSource source, String[] args) {
        if (!(source instanceof Player)) {
            source.sendMessage(verifier.getConfig().notFromConsole());
            return;
        }

        Config config = verifier.getConfig();
        VerifiableUserManager userManager = verifier.getUserManager();

        VerifiableUser user =
                userManager.create(((Player) source).getUniqueId());
        if (user.getDiscordId().isEmpty()) {
            source.sendMessage(config.notVerified());
            return;
        }
        verifier.getVerificationManager().removeRoles((Player) source);
        userManager.updateUser(user.toBuilder()
                .discordId(null)
                .discordName(null)
                .verified(false)
                .build());
        source.sendMessage(config.verificationCanceled());
    }

    @Override
    protected @NotNull List<String> handleSuggestion(CommandSource source, String[] args) {
        return Collections.emptyList();
    }

}
