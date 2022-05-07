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
import org.jetbrains.annotations.NotNull;
import xyz.yawek.discordverifier.DiscordVerifier;

import java.util.Collections;
import java.util.List;

public abstract class PermissibleCommand implements ExecutableCommand {

    protected final DiscordVerifier verifier;
    private final String permission;

    public PermissibleCommand(DiscordVerifier verifier, String permission) {
        this.verifier = verifier;
        this.permission = permission;
    }

    @Override
    public void execute(CommandSource source, String[] args) {
        if (!source.hasPermission(permission)) {
            source.sendMessage(verifier.getConfig().noPermission());
            return;
        }
        handle(source, args);
    }

    @Override
    public @NotNull List<String> suggest(CommandSource source, String[] args) {
        if (!source.hasPermission(permission)) return Collections.emptyList();
        return handleSuggestion(source, args);
    }

    public String getPermission() {
        return permission;
    }

    protected abstract void handle(CommandSource source, String[] args);

    protected abstract @NotNull List<String> handleSuggestion(CommandSource source, String[] args);

}
