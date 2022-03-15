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
