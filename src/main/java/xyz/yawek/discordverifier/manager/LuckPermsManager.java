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

package xyz.yawek.discordverifier.manager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.util.LogUtils;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

public class LuckPermsManager {

    private final DiscordVerifier verifier;
    private LuckPerms luckPerms;

    public LuckPermsManager(DiscordVerifier verifier) {
        this.verifier = verifier;
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            LogUtils.info("LuckPerms not found, some functions will be limited.");
        }
    }

    public void reloadPerms() {
        DiscordManager discord = verifier.getDiscordManager();

        LinkedHashMap<String, String> roleMap = verifier.getConfig().groupsRoles();
        roleMap.forEach((groupName, roleId) -> {
            Optional<Role> roleOptional = discord.getRole(roleId);
            if (roleOptional.isEmpty()) return;

            discord.getPlayersWithRole(roleId).forEach(user -> {
                if (user.getDiscordId().isEmpty()) return;
                Optional<Member> memberOptional =
                        discord.getMemberById(user.getDiscordId().get());
                if (memberOptional.isEmpty()) return;

                luckPerms.getUserManager().loadUser(user.getUUID()).thenAccept(lpUser -> {
                    boolean hasPermission = lpUser.getNodes()
                            .stream()
                            .anyMatch(node -> {
                                if (node.getKey().equals("group." + groupName)) {
                                    Set<String> values = node.getContexts().getValues("server");
                                    return values.size() == 0 || values.contains("bungee");
                                }
                                return false;
                            });
                    if (!hasPermission) {
                        discord.removeRole(memberOptional.get(), roleOptional.get());
                    }
                });
            });
        });
    }

}
