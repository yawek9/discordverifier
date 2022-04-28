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
import xyz.yawek.discordverifier.config.ConfigProvider;
import xyz.yawek.discordverifier.role.GroupRole;
import xyz.yawek.discordverifier.user.VerifiableUser;
import xyz.yawek.discordverifier.utils.MessageUtils;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VerificationManager {

    private final DiscordVerifier verifier;
    private final ConcurrentHashMap<Player, Member> verifyingPlayers = new ConcurrentHashMap<>();

    public VerificationManager(DiscordVerifier verifier) {
        this.verifier = verifier;
    }

    public void startVerification(Member member, Player player) {
        verifyingPlayers.put(player, member);
        MessageUtils.sendMessageFromConfig(
                player,
                "AcceptVerification",
                true,
                member.getUser().getAsTag());
        verifier.getServer().getScheduler()
                .buildTask(DiscordVerifier.getVerifier(),
                        () -> cancelVerification(member, player))
                .delay(verifier.getConfigProvider().getInt(
                        "VerificationExpireAfter"), TimeUnit.SECONDS)
                .schedule();
    }

    public void cancelVerification(Member member, Player player) {
        if (player.isActive()) {
            MessageUtils.sendMessageFromConfig(
                    player,
                    "VerificationExpired",
                    true,
                    member.getUser().getAsTag());
            verifyingPlayers.remove(player);
        }
    }

    public void completeVerification(Player player, boolean accepted) {
        ConfigProvider config = verifier.getConfigProvider();

        if (!verifyingPlayers.containsKey(player)) {
            MessageUtils.sendMessageFromConfig(
                    player,
                    "NoVerificationRequired",
                    true);
            return;
        }
        if (!player.isActive()) {
            return;
        }
        if (!accepted) {
            MessageUtils.sendMessageFromConfig(
                    player,
                    "VerificationDenied",
                    true);
            verifier.getDiscordManager().sendVerificationEmbed(
                    config.getString("VerificationDeniedTitle"),
                    config.getString("VerificationDeniedBody"),
                    config.getString("VerificationDeniedFooter"));
            verifyingPlayers.remove(player);
            return;
        }

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        Member member = verifyingPlayers.get(player);
        user.setVerified(true);
        user.setDiscordId(member.getId());
        user.setDiscordName(member.getUser().getAsTag());
        verifier.getDataProvider().updateUser(user);

        updateRoles(player);
        updateNickname(player);

        MessageUtils.sendMessageFromConfig(
                player,
                "VerifiedSuccessfully",
                true,
                verifyingPlayers.get(player).getUser().getAsTag()
        );
        verifier.getDiscordManager().sendVerificationEmbed(
                config.getString("VerificationSuccessfulTitle"),
                config.getString("VerificationSuccessfulBody"),
                config.getString("VerificationSuccessfulFooter"));

        verifyingPlayers.remove(player);
    }

    @SuppressWarnings("unchecked")
    public void updateRoles(Player player) {
        ConfigProvider config = verifier.getConfigProvider();
        DiscordManager discord = verifier.getDiscordManager();

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        if (user.getDiscordId().isEmpty()) return;
        Optional<Member> memberOptional = discord.getMemberById(user.getDiscordId().get());
        if (memberOptional.isEmpty()) return;

        LinkedHashMap<String, String> roleMap =
                (LinkedHashMap<String, String>) config.getMap("Roles");
        Set<GroupRole> roleSet = new TreeSet<>();

        roleMap.forEach((groupName, roleId) -> {
            Optional<Role> roleOptional = verifier.getDiscordManager().getRole(roleId);
            roleOptional.ifPresent(role -> roleSet.add(new GroupRole(groupName, role)));
        });

        Member member = memberOptional.get();
        if (!user.isVerified()) {
            roleSet.stream()
                    .filter(groupRole ->
                            groupRole.getGroupName().equals("default"))
                    .findAny()
                    .ifPresent(groupRole ->
                            discord.addRole(member, groupRole.getRole()));
        }
        boolean roleAssigned = false;
        for (GroupRole groupRole : roleSet) {
            Role role = groupRole.getRole();
            if (player.getPermissionValue("group." + groupRole.getGroupName())
                    .equals(Tristate.TRUE)
                    && (!roleAssigned ||
                    !config.getBoolean("LimitRolesToOne"))) {
                discord.addRole(member, role);
                roleAssigned = true;
            } else {
                discord.removeRole(member, role);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void removeRoles(Player player) {
        ConfigProvider config = verifier.getConfigProvider();

        VerifiableUser user = verifier.getUserManager().create(player.getUniqueId());
        if (user.getDiscordId().isEmpty()) return;

        LinkedHashMap<String, String> roleMap =
                (LinkedHashMap<String, String>) config.getMap("Roles");
        Set<GroupRole> roleSet = new TreeSet<>();
        roleMap.forEach((groupName, roleId) -> {
            Optional<Role> roleOptional = verifier.getDiscordManager().getRole(roleId);
            roleOptional.ifPresent(role -> roleSet.add(new GroupRole(groupName, role)));
        });

        DiscordManager discordManager = verifier.getDiscordManager();
        Optional<Member> memberOptional =
                discordManager.getMemberById(user.getDiscordId().get());
        if (memberOptional.isEmpty()) return;
        roleSet.forEach(groupRole -> discordManager.removeRole(
                memberOptional.get(), groupRole.getRole()));
    }

    public void updateNickname(Player player) {
        ConfigProvider config = verifier.getConfigProvider();

        if (!config.getBoolean("ForceNicknamesOnDiscord")) {
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
        if (!memberOptional.get().getPermissions().contains(Permission.ADMINISTRATOR)) {
            discordManager.setNickname(memberOptional.get(), player.getUsername());
        }
    }

}