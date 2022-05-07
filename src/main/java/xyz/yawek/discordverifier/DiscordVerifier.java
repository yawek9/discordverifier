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
import xyz.yawek.discordverifier.command.CommandHandler;
import xyz.yawek.discordverifier.config.Config;
import xyz.yawek.discordverifier.config.ConfigProvider;
import xyz.yawek.discordverifier.data.DataProvider;
import xyz.yawek.discordverifier.discordlistener.MessageReceivedListener;
import xyz.yawek.discordverifier.listener.LoginListener;
import xyz.yawek.discordverifier.manager.DiscordManager;
import xyz.yawek.discordverifier.manager.LuckPermsManager;
import xyz.yawek.discordverifier.manager.VerifiableUserManager;
import xyz.yawek.discordverifier.manager.VerificationManager;

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
public class DiscordVerifier {

    private static DiscordVerifier plugin;

    public static final String VERSION = "1.0.6";

    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;
    private Config config;
    private ConfigProvider configProvider;
    private DataProvider dataProvider;
    private DiscordManager discordManager;
    private VerifiableUserManager userManager;
    private VerificationManager verificationManager;

    @Inject
    public DiscordVerifier(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        plugin = this;

        this.configProvider = new ConfigProvider(this);
        configProvider.loadConfig();
        this.config = new Config(configProvider);

        this.dataProvider = new DataProvider(this);
        dataProvider.setup();

        this.userManager = new VerifiableUserManager(this);

        this.discordManager = new DiscordManager(this);
        if (discordManager.setup()) {
            discordManager.addEventListener(new MessageReceivedListener(this));
            new LuckPermsManager(this).reloadPerms();
        }

        this.verificationManager = new VerificationManager(this);

        server.getEventManager().register(this, new LoginListener(this));

        CommandMeta meta = server.getCommandManager()
                .metaBuilder("discord").build();
        server.getCommandManager().register(meta, new CommandHandler(this));
    }

    public void reload() {
        configProvider.loadConfig();
        dataProvider.setup();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onShutdown(ProxyShutdownEvent e) {
        dataProvider.shutdown();
        discordManager.shutdown();
    }

    public static DiscordVerifier getVerifier() {
        return plugin;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Config getConfig() {
        return config;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public VerifiableUserManager getUserManager() {
        return userManager;
    }

    public VerificationManager getVerificationManager() {
        return verificationManager;
    }

}
