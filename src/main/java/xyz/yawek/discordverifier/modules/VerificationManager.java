package xyz.yawek.discordverifier.modules;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.player.PlayerData;
import xyz.yawek.discordverifier.utils.VelocityMessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VerificationManager {

    private static ConcurrentHashMap<Player, Member> verificationPlayers = new ConcurrentHashMap<>();

    public static void startVerificationProcess(Member member, Player player) {
        if (!verificationPlayers.containsKey(player)) {
            return;
        }

        VelocityMessageUtils.sendMessageFromConfig(
                player,
                "AcceptVerification",
                true,
                member.getUser().getAsTag()
        );

        VelocityDiscordVerifier.getServer().getScheduler().buildTask(VelocityDiscordVerifier.getInstance(), () -> {
            cancelVerificationProcess(member, player);
        }).delay(VelocityConfigManager.getInt("VerificationExpireAfter"), TimeUnit.SECONDS).schedule();
    }

    public static void cancelVerificationProcess(Member member, Player player) {
        if (player.isActive()) {
            VelocityMessageUtils.sendMessageFromConfig(
                    player,
                    "VerificationExpired",
                    true,
                    member.getUser().getAsTag()
            );
            verificationPlayers.remove(player);
        }
    }

    public static void completeVerificationProcess(Player player, boolean accepted) {
        if (!verificationPlayers.containsKey(player)) {
            VelocityMessageUtils.sendMessageFromConfig(
                    player,
                    "NoVerificationRequired",
                    true
            );
            return;
        }

        if (!player.isActive()) {
            return;
        }

        if (!accepted) {
            VelocityMessageUtils.sendMessageFromConfig(
                    player,
                    "VerificationDenied",
                    true
            );

            JDAManager.sendEmbedMessageInVerifyChannel(
                    VelocityConfigManager.getString("VerificationDeniedTitle"),
                    VelocityConfigManager.getString("VerificationDeniedBody"),
                    VelocityConfigManager.getString("VerificationDeniedFooter")
            );

            verificationPlayers.remove(player);
            return;
        }

        PlayerData playerData = new PlayerData(player.getUniqueId());

        playerData.setVerified(true);
        playerData.setDiscordId(verificationPlayers.get(player).getId());
        playerData.setDiscordName(verificationPlayers.get(player).getUser().getAsTag());

        updateRoles(player);
        updateNickname(player);

        VelocityMessageUtils.sendMessageFromConfig(
                player,
                "VerifiedSuccessfully",
                true,
                verificationPlayers.get(player).getUser().getAsTag()
        );

        JDAManager.sendEmbedMessageInVerifyChannel(
                VelocityConfigManager.getString("VerificationSuccessfulTitle"),
                VelocityConfigManager.getString("VerificationSuccessfulBody"),
                VelocityConfigManager.getString("VerificationSuccessfulFooter")
        );

        verificationPlayers.remove(player);
    }

    public static void setVerificationPlayer(Player player, Member member) {
        verificationPlayers.put(player, member);
    }

    public static void updateRoles(Player player) {
        PlayerData playerData = new PlayerData(player.getUniqueId());

        if (!playerData.isVerified()) return;

        Member member = JDAManager.getMemberById(playerData.getDiscordId());

        if (member == null) return;

        LinkedHashMap<String, String> groupsRoles =
                (LinkedHashMap<String, String>) VelocityConfigManager.getMap("Roles");

        List<Role> roles = new ArrayList<>();
        LinkedList<Role> sortedRoles = new LinkedList<>();

        for (String s : groupsRoles.keySet()) {
            roles.add(JDAManager.getRole(groupsRoles.get(s)));
        }

        int index1 = 0;

        for (Role r : roles) {
            if (sortedRoles.size() == 0) {
                sortedRoles.add(r);
                continue;
            }

            int index2 = 0;
            for (Role rr : sortedRoles) {
                if (r.getPosition() > rr.getPosition()) {
                    sortedRoles.add(index2, r);
                    break;
                }
                index2++;
            }

            if (index1 == sortedRoles.size() - 1) sortedRoles.add(r);
            index1++;
        }

        boolean roleAssigned = false;

        for (Role r : sortedRoles) {
            if (player.getPermissionValue("group." + getKey(groupsRoles, r.getId())).equals(Tristate.TRUE)
                    && (!roleAssigned || !VelocityConfigManager.getBoolean("LimitRolesToOne"))) {
                JDAManager.addRole(member, r);
                roleAssigned = true;
            } else {
                JDAManager.removeRole(member, r);
            }
        }
    }

    public static void removeRoles(Player player) {
        PlayerData playerData = new PlayerData(player.getUniqueId());

        LinkedHashMap<String, String> roles =
                (LinkedHashMap<String, String>) VelocityConfigManager.getMap("Roles");

        for (String s : roles.keySet()) {
            JDAManager.removeRole(JDAManager.getMemberById(
                    playerData.getDiscordId()),
                    JDAManager.getRole(roles.get(s)
            ));
        }
    }

    public static void updateNickname(Player player) {
        PlayerData playerData = new PlayerData(player.getUniqueId());

        if (!playerData.isVerified()) {
            return;
        }

        if (!VelocityConfigManager.getBoolean("ForceNicknamesOnDiscord")) {
            return;
        }

        if (JDAManager.getMemberById(playerData.getDiscordId()) == null) return;

        if (!JDAManager.getMemberById(playerData.getDiscordId()).getPermissions().contains(Permission.ADMINISTRATOR)) {
            JDAManager.setNickname(JDAManager.getMemberById(playerData.getDiscordId()), player.getUsername());
        }
    }

    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}