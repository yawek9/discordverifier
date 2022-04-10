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

package xyz.yawek.discordverifier.utils;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;

public class VelocityMessageUtils {

    public static void sendMessageFromConfig(CommandSource source, String key, boolean prefix, String... vars) {
        String message = VelocityConfigManager.getString(key);

        if (vars != null) {
            for (int i = 1; i <= vars.length; i++) {
                if (i == 1) {
                    message = message.replaceAll("%VAR%", vars[i - 1]);
                    continue;
                }

                message = message.replaceAll("%VAR" + i + "%", vars[i - 1]);
            }
        }

        if (prefix) {
            source.sendMessage(ColorUtils.decorate(Component.text(
                    VelocityConfigManager.getString("Prefix") + message
            )));
        } else {
            source.sendMessage(ColorUtils.decorate(Component.text(message)));
        }
    }

}
