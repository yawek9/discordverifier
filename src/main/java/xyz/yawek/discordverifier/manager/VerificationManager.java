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

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import xyz.yawek.discordverifier.DiscordVerifier;
import xyz.yawek.discordverifier.config.Config;
import xyz.yawek.discordverifier.role.GroupRole;
import xyz.yawek.discordverifier.user.VerifiableUser;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VerificationManager {

    private final DiscordVerifier verifier;
    private final ConcurrentHashMap<Player, Member> verifyingPlayers = new ConcurrentHashMap<>();

    public VerificationManager(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public boolean startVerification(Member member, Player player) {
        Config config = verifier.getConfig();

        if (verifyingPlayers.containsKey(player)) return false;

        verifyingPlayers.put(player, member);
        player.sendMessage(config.verificationRequest(member.getUser().getAsTag()));
        verifier.getServer().getScheduler()
                .buildTask(DiscordVerifier.getVerifier(),
                        () -> cancelVerification(member, player))
                .delay(config.verificationExpireTime(), TimeUnit.SECONDS)
                .schedule();
        return true;
    }

    public void cancelVerification(Member member, Player player) {
        if (player.isActive() && verifyingPlayers.containsKey(player)) {
            player.sendMessage(verifier.getConfig()
                    .verificationExpired(member.getUser().getAsTag()));
            verifyingPlayers.remove(player);
        }
    }

    public void completeVerification(Player player, boolean accepted) {
        Config config = verifier.getConfig();

        if (!verifyingPlayers.containsKey(player)) {
            player.sendMessage(config.noRequests());
            return;
        }
        if (!player.isActive()) {
            return;
        }
        if (!accepted) {
            player.sendMessage(config.verificationDenied());
            verifier.getDiscordManager().sendInVerification(
                    config.verificationDenied(player.getUsername()));
            verifyingPlayers.remove(player);
            return;
        }

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        Member member = verifyingPlayers.get(player);
        verifier.getDataProvider().updateUser(user.toBuilder()
                .verified(true)
                .discordId(member.getId())
                .discordName(member.getUser().getAsTag())
                .build());

        updateRoles(player);
        updateNickname(player);

        player.sendMessage(config.verifiedSuccessfully(verifyingPlayers.get(player).getUser().getAsTag()));
        verifier.getDiscordManager().sendInVerification(config.verificationSuccess());
        verifyingPlayers.remove(player);
    }

    public void updateRoles(Player player) {
        Config config = verifier.getConfig();
        DiscordManager discord = verifier.getDiscordManager();

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        if (user.getDiscordId().isEmpty()) return;
        Optional<Member> memberOptional = discord.getMemberById(user.getDiscordId().get());
        if (memberOptional.isEmpty()) return;

        Set<GroupRole> roleSet = config.groupsRoles().entrySet()
                .stream().map(entry -> {
                    Optional<Role> roleOptional =
                            verifier.getDiscordManager().getRole(entry.getValue());
                    if (roleOptional.isEmpty()) return null;
                    return new GroupRole(entry.getKey(), roleOptional.get());
                }).filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        Member member = memberOptional.get();
        boolean roleAssigned = false;
        for (GroupRole groupRole : roleSet) {
            Role role = groupRole.getRole();
            if (player.getPermissionValue("group." + groupRole.getGroupName())
                    .equals(Tristate.TRUE) && (!roleAssigned || !config.oneRoleLimit())) {
                discord.addRole(member, role);
                roleAssigned = true;
            } else {
                discord.removeRole(member, role);
            }
        }
    }

    public void removeRoles(Player player) {
        Config config = verifier.getConfig();

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        if (user.getDiscordId().isEmpty()) return;

        Set<GroupRole> roleSet = config.groupsRoles().entrySet()
                .stream().map(entry -> {
                    Optional<Role> roleOptional =
                            verifier.getDiscordManager().getRole(entry.getValue());
                    if (roleOptional.isEmpty()) return null;
                    return new GroupRole(entry.getKey(), roleOptional.get());
                }).filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        DiscordManager discordManager = verifier.getDiscordManager();
        Optional<Member> memberOptional =
                discordManager.getMemberById(user.getDiscordId().get());
        if (memberOptional.isEmpty()) return;
        roleSet.forEach(groupRole -> discordManager.removeRole(
                memberOptional.get(), groupRole.getRole()));
    }

    public void updateNickname(Player player) {
        Config config = verifier.getConfig();

        if (!config.forceNicknames()) {
            return;
        }
        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        if (!user.isVerified() || user.getDiscordId().isEmpty()) {
            return;
        }
        DiscordManager discordManager = verifier.getDiscordManager();
        Optional<Member> memberOptional =
                discordManager.getMemberById(user.getDiscordId().get());
        if (memberOptional.isEmpty()) return;
        if (!memberOptional.get().getPermissions().contains(Permission.NICKNAME_CHANGE)) {
            discordManager.setNickname(memberOptional.get(), player.getUsername());
        }
    }

}