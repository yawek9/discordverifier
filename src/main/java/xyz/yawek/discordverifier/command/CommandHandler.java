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
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.command.subcommand.*;
import xyz.yawek.discordverifier.config.Config;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CommandHandler implements SimpleCommand {

    private final DiscordVerifier verifier;
    private final Map<String, ExecutableCommand> commandMap = new HashMap<>();

    public CommandHandler(DiscordVerifier verifier) {
        this.verifier = verifier;

        commandMap.put("reload", new ReloadCommand(verifier));
        commandMap.put("info", new InfoCommand(verifier));
        commandMap.put("accept", new AcceptCommand(verifier));
        commandMap.put("deny", new DenyCommand(verifier));
        commandMap.put("unlink", new UnlinkCommand(verifier));
    }

    @Override
    public void execute(Invocation invocation) {
        Config config = verifier.getConfig();

        CommandSource source = invocation.source();
        if (!source.hasPermission("discordverifier.discord")) {
            source.sendMessage(config.noPermission());
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0 || !commandMap.containsKey(args[0])) {
            source.sendMessage(config.discordInfo());
            return;
        }
        commandMap.get(args[0]).execute(source,
                Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 1) {
            List<String> firstArguments = new ArrayList<>();
            for (String commandString : commandMap.keySet()) {
                if (commandMap.get(commandString)
                        instanceof PermissibleCommand permissibleCommand) {
                    if (source.hasPermission(permissibleCommand.getPermission()))
                        firstArguments.add(commandString);
                } else {
                    firstArguments.add(commandString);
                }
            }
            return CompletableFuture.completedFuture(firstArguments);
        } else if (args.length > 1 && commandMap.containsKey(args[0])) {
            return CompletableFuture.completedFuture(commandMap.get(args[0]).suggest(source,
                    Arrays.copyOfRange(args, 1, args.length)));
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
