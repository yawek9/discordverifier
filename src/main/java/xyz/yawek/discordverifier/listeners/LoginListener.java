package xyz.yawek.discordverifier.listeners;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import xyz.yawek.discordverifier.data.DataManager;
import xyz.yawek.discordverifier.modules.VerificationManager;
import xyz.yawek.discordverifier.player.PlayerData;
import xyz.yawek.discordverifier.utils.VelocityMessageUtils;

public class LoginListener {

    @Subscribe
    public EventTask onPlayerLogin(LoginEvent e) {
        return EventTask.async(() -> {
            DataManager.createOrUpdatePlayerData(e.getPlayer().getUniqueId(), e.getPlayer().getUsername());

            VerificationManager.updateRoles(e.getPlayer());
            VerificationManager.updateNickname(e.getPlayer());

            PlayerData playerData = new PlayerData(e.getPlayer().getUniqueId());

            if (!playerData.isVerified())
                VelocityMessageUtils.sendMessageFromConfig(
                        e.getPlayer(),
                        "NotVerifiedYet",
                        true
                );
        });
    }

}
