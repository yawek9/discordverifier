package xyz.yawek.discordverifier.modules;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import xyz.yawek.discordverifier.VelocityDiscordVerifier;
import xyz.yawek.discordverifier.data.DataManager;
import xyz.yawek.discordverifier.player.PlayerData;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class JDAManager {

    private static JDA jda;
    private static final long GUILD_ID = Long.parseLong(VelocityConfigManager.getString("GuildID"));

    public static void initializeJda() {
        try {
            jda = JDABuilder.create(
                    VelocityConfigManager.getString("BotToken"),
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGE_TYPING,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_BANS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_INVITES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_TYPING,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_VOICE_STATES
            ).build();

            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getDiscordName(String userId) {
        try {
            return jda.getGuildById(GUILD_ID).getMemberById(userId).getEffectiveName();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static boolean isBotItself(User user) {
        return jda.getSelfUser() == user;
    }

    public static boolean isOtherBot(User user) {
        return user.isBot() && jda.getSelfUser() != user;
    }

    public static void sendEmbedMessage(TextChannel textChannel, String title, String body, String footer) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setFooter(footer);

        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void sendEmbedMessageInVerifyChannel(String title, String body, String footer) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setFooter(footer);

        jda.getTextChannelById(VelocityConfigManager.getString("VerificationChannelID"))
                .sendMessageEmbeds(embedBuilder.build())
                .queue();
    }

    public static void addEventListener(Object object) {
        jda.addEventListener(object);
    }

    public static Role getRole(String id) {
        return jda.getRoleById(id);
    }

    public static void addRole(Member member, Role role) {
        try {
            Guild guild = jda.getGuildById(VelocityConfigManager.getString("GuildID"));
            guild.addRoleToMember(member, role).queue();
        } catch (NullPointerException e) {
            VelocityDiscordVerifier.getLogger().error("Role with ID " + role.getId() + " does not exists.");
        }
    }

    public static void removeRole(Member member, Role role) {
        try {
            Guild guild = jda.getGuildById(VelocityConfigManager.getString("GuildID"));
            if (member == null) return;
            guild.removeRoleFromMember(member, role).queue();
        } catch (NullPointerException e) {
            VelocityDiscordVerifier.getLogger().error("Role with ID " + role.getId() + " does not exists.");
        }
    }

    public static Member getMemberById(String memberId) {
        try {
            return jda.getGuildById(VelocityConfigManager.getString("GuildID")).getMemberById(memberId);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static void setNickname(Member member, String nickname) {
        if (member == null) return;
        member.modifyNickname(nickname).queue();
    }

    public static List<PlayerData> getPlayersWithRole(String roleId) {
        List<Member> members = jda.getGuildById(GUILD_ID).getMembersWithRoles(getRole(roleId));

        List<PlayerData> playersData = new ArrayList<>();

        for (Member member : members) {
            if (DataManager.getUUIDByDiscordId(member.getId()) == null) continue;
            playersData.add(new PlayerData(DataManager.getUUIDByDiscordId(member.getId())));
        }

        return playersData;
    }
    
}
