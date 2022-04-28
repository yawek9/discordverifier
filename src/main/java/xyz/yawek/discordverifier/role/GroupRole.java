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

package xyz.yawek.discordverifier.role;

import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

public class GroupRole implements Comparable<GroupRole> {

    private final String groupName;
    private final Role role;

    public GroupRole(String groupName, Role role) {
        this.groupName = groupName;
        this.role = role;
    }

    public String getGroupName() {
        return groupName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public int compareTo(@NotNull GroupRole r) {
        return Integer.compare(r.getRole().getPosition(), this.getRole().getPosition());
    }

}
