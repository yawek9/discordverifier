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

import org.slf4j.Logger;
import xyz.yawek.discordverifier.DiscordVerifier;

public class LogUtils {

    private static final Logger LOGGER = DiscordVerifier.getVerifier().getLogger();

    public static void info(String text) {
        LOGGER.info(text);
    }

    public static void error(String text) {
        LOGGER.error(text);
    }

    public static void infoDataAccess(String text) {
        LOGGER.info("[Data] " + text);
    }

    public static void errorDataAccess(String text) {
        LOGGER.error("[Data] " + text);
    }

    public static void errorDataAccess(String text, String... arguments) {
        LOGGER.error("[Data] " + text, (Object[]) arguments);
    }

    public static void errorDiscord(String text, String... arguments) {
        LOGGER.error("[Discord] " + text, (Object[]) arguments);
    }

}
