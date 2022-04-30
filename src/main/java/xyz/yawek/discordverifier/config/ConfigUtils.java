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

import net.kyori.adventure.text.Component;
import xyz.yawek.discordverifier.util.ColorUtils;

public class ConfigUtils {

    private final ConfigProvider configProvider;

    public ConfigUtils(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public String stringWithArgs(String key, String... arguments) {
        String message = configProvider.getString(key);
        if (message == null) return null;
        for (String arg : arguments) {
            message = message.replaceFirst("\\{}", arg);
        }
        return message;
    }

    public Component prefixedMessage(String key) {
        String message = configProvider.getString(key);
        if (message == null) return null;
        return ColorUtils.decorate(Component.text(
                configProvider.getString("messages.chat.prefix")
                        + message));
    }

    public Component prefixedMessage(String key, String... arguments) {
        String message = stringWithArgs(key, arguments);
        return ColorUtils.decorate(Component.text(
                configProvider.getString("messages.chat.prefix") + message));
    }

    public Component noPrefixMessage(String key) {
        String message = configProvider.getString(key);
        if (message == null) return null;
        return ColorUtils.decorate(Component.text(message));
    }

    public Component listPrefixedMessage(String key) {
        return ColorUtils.decorate(Component.text(
                configProvider.getString("messages.chat.prefix")
                        + String.join("\n", configProvider.getStringList(key))));
    }

    public Component listPrefixedMessage(String key, String... arguments) {
        String message = configProvider.getString("messages.chat.prefix")
                + String.join("\n", configProvider.getStringList(key));
        for (String arg : arguments) {
            message = message.replaceFirst("\\{}", arg);
        }
        return ColorUtils.decorate(Component.text(message));
    }

}
