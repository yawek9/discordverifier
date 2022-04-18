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

package xyz.yawek.discordverifier;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import xyz.yawek.discordverifier.commands.DiscordCommand;
import xyz.yawek.discordverifier.data.DataManager;
import xyz.yawek.discordverifier.discordlisteners.MessageReceivedListener;
import xyz.yawek.discordverifier.listeners.LoginListener;
import xyz.yawek.discordverifier.modules.JDAManager;
import xyz.yawek.discordverifier.modules.LuckPermsModule;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;

import java.nio.file.Path;

@Plugin(id = "discordverifier",
        name = "DiscordVerifier",
        version = "1.0.6",
        url = "https://yawek.xyz",
        description = "Simple Velocity Discord account linking plugin.",
        authors = {"yawek9"},
        dependencies = {
            @Dependency(id ="luckperms", optional = true)
        }
)
public class VelocityDiscordVerifier {

    public static final String VERSION = "1.0.6";

    private static VelocityDiscordVerifier plugin;
    private static Logger logger;
    private static Path dataDirectory;
    private static ProxyServer server;

    @Inject
    public VelocityDiscordVerifier(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        VelocityDiscordVerifier.server = server;
        VelocityDiscordVerifier.logger = logger;
        VelocityDiscordVerifier.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        plugin = this;

        VelocityConfigManager.loadConfig();

        DataManager.setupDataManager();

        server.getEventManager().register(this, new LoginListener());

        CommandMeta meta = server.getCommandManager().metaBuilder("discord").build();

        server.getCommandManager().register(meta, new DiscordCommand());

        JDAManager.initializeJda();
        JDAManager.addEventListener(new MessageReceivedListener());

        LuckPermsModule.loadLuckPerms();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent e) {
        DataManager.shutdownDataManager();
    }

    public static ProxyServer getServer() {
        return server;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Path getDataDirectory() {
        return dataDirectory;
    }

    public static VelocityDiscordVerifier getInstance() {
        return plugin;
    }

}
