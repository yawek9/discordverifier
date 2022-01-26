package xyz.yawek.discordverifier.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;
import xyz.yawek.discordverifier.modules.VerificationManager;
import xyz.yawek.discordverifier.player.PlayerData;
import xyz.yawek.discordverifier.utils.VelocityMessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DiscordCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            if (args.length < 1) {
                VelocityMessageUtils.sendMessageFromConfig(source, "AdminCommandUsage", true);
                return;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                VelocityConfigManager.loadConfig();
                VelocityMessageUtils.sendMessageFromConfig(source, "ConfigReloaded", true);
                return;
            }

            PlayerData playerData = new PlayerData(args[1]);

            if (playerData.getUUID() == null) {
                VelocityMessageUtils.sendMessageFromConfig(source, "PlayerNotFound", true);
                return;
            }

            if (playerData.getDiscordId() == null) {
                VelocityMessageUtils.sendMessageFromConfig(source, "PlayerNotVerified", true);
                return;
            }

            String isOnline;

            if (playerData.isOnline()) {
                isOnline = VelocityConfigManager.getString("Online");
            } else {
                isOnline = VelocityConfigManager.getString("Offline");
            }

            VelocityMessageUtils.sendMessageFromConfig(
                    source,
                    "PlayerInfo",
                    true,
                    playerData.getLastNickname(),
                    playerData.getUUID().toString(),
                    playerData.getDiscordId(),
                    playerData.getDiscordName(),
                    isOnline
            );

            return;
        }


        if (source.hasPermission("discordverifier.admin")) {
            if (args.length < 2) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("accept")) {
                        VerificationManager.completeVerificationProcess((Player) source, true);
                        return;
                    }

                    if (args[0].equalsIgnoreCase("deny")) {
                        VerificationManager.completeVerificationProcess((Player) source, false);
                        return;
                    }

                    if (args[0].equalsIgnoreCase("unlink")) {
                        PlayerData playerData = new PlayerData(((Player) source).getUniqueId());

                        if (playerData.getDiscordId() == null) {
                            VelocityMessageUtils.sendMessageFromConfig(source, "NotVerified", true);
                            return;
                        }

                        VerificationManager.removeRoles((Player) source);

                        playerData.setVerified(false);
                        playerData.setDiscordName(null);
                        playerData.setDiscordId(null);

                        VelocityMessageUtils.sendMessageFromConfig(source, "UnverifiedSuccesfully", true);

                        return;
                    }
                }

                VelocityMessageUtils.sendMessageFromConfig(source, "AdminCommandUsage", true);
                return;
            }

            PlayerData playerData = new PlayerData(args[1]);

            if (playerData.getUUID() == null) {
                VelocityMessageUtils.sendMessageFromConfig(source, "PlayerNotFound", true);
                return;
            }

            if (playerData.getDiscordId() == null) {
                VelocityMessageUtils.sendMessageFromConfig(source, "PlayerNotVerified", true);
                return;
            }

            String isOnline;

            if (playerData.isOnline()) {
                isOnline = VelocityConfigManager.getString("Online");
            } else {
                isOnline = VelocityConfigManager.getString("Offline");
            }

            VelocityMessageUtils.sendMessageFromConfig(
                    source,
                    "PlayerInfo",
                    true,
                    playerData.getLastNickname(),
                    playerData.getUUID().toString(),
                    playerData.getDiscordId(),
                    playerData.getDiscordName(),
                    isOnline
            );

            return;
        }

        if (args.length == 0) {
            VelocityMessageUtils.sendMessageFromConfig(source, "DiscordInfo", true);
            return;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            VerificationManager.completeVerificationProcess((Player) source, true);
            return;
        }

        if (args[0].equalsIgnoreCase("deny")) {
            VerificationManager.completeVerificationProcess((Player) source, false);
            return;
        }

        if (args[0].equalsIgnoreCase("unlink")) {
            PlayerData playerData = new PlayerData(((Player) source).getUniqueId());

            if (playerData.getDiscordId() == null) {
                VelocityMessageUtils.sendMessageFromConfig(source, "NotVerified", true);
                return;
            }

            VerificationManager.removeRoles((Player) source);

            playerData.setVerified(false);
            playerData.setDiscordName(null);
            playerData.setDiscordId(null);

            VelocityMessageUtils.sendMessageFromConfig(source, "UnverifiedSuccesfully", true);

            return;
        }

        VelocityMessageUtils.sendMessageFromConfig(source, "DiscordInfo", true);
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
