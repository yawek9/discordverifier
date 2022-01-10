package xyz.yawek.discordverifier.utils;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import xyz.yawek.discordverifier.modules.VelocityConfigManager;

public class VelocityMessageUtils {

    public static void sendMessageFromConfig(CommandSource source, String key, boolean prefix) {
        if (prefix) {
            source.sendMessage(ColorUtils.decorate(Component.text(
                    VelocityConfigManager.getString("Prefix") + VelocityConfigManager.getString(key)
            )));
        } else {
            source.sendMessage(ColorUtils.decorate(Component.text(VelocityConfigManager.getString(key))));
        }
    }

    public static void sendMessageFromConfig(CommandSource source, String key, boolean prefix, String... vars) {
        String message = VelocityConfigManager.getString(key);

        for (int i = 1; i <= vars.length; i++) {
            if (i == 1) {
                message = message.replaceAll("%VAR%", vars[i - 1]);
                continue;
            }

            message = message.replaceAll("%VAR" + i + "%", vars[i - 1]);
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
