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

package xyz.yawek.discordverifier.modules;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.data.DataManager;

import java.util.LinkedHashMap;
import java.util.Set;

public class LuckPermsModule {

    private static LuckPerms luckPerms;

    public static void loadLuckPerms() {
        try {
            luckPerms = LuckPermsProvider.get();
            reloadPerms();
        } catch (IllegalStateException e) {
            VelocityDiscordVerifier.getLogger().info("LuckPerms not found, some functions will be limited.");
        }
    }

    private static void reloadPerms() {
        LinkedHashMap<String, String> groupsRoles =
                (LinkedHashMap<String, String>) VelocityConfigManager.getMap("Roles");

        for (String groupName : groupsRoles.keySet()) {
            String roleId = groupsRoles.get(groupName);

            JDAManager.getPlayersWithRole(groupsRoles.get(groupName)).forEach(playerData -> {
                    luckPerms.getUserManager().loadUser(playerData.getUUID()).thenAccept(user -> {
                        if (user == null) return;

                        boolean hasPermission = user.getNodes()
                                .stream()
                                .anyMatch(node -> {
                                    if (node.getKey().equals("group." + groupName)) {
                                        Set<String> values = node.getContexts().getValues("server");
                                        return values.size() == 0 || values.contains("bungee");
                                    }
                                    return false;
                                });

                        if (!hasPermission) {
                            JDAManager.removeRole(
                                    JDAManager.getMemberById(DataManager.getDiscordId(playerData.getUUID())),
                                    JDAManager.getRole(roleId));
                        }
                    });
            });
        }
    }

}
