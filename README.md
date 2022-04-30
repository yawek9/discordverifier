# DiscordVerifier

Simple Velocity server proxy plugin that links server groups to the Discord roles.

# Requirements
- Java 17
- Discord bot with enabled intents

# Usage

## How to get started?
When you put plugin JAR file in the plugins folder, you have to enable the server to generate the config file and then shut it off.
After setting everything up in the config.yml and enabling the servers, if all is configured well, you can try to get verified.
To do this, enter the server and type in verification channel '!verify (your nickname)'.
After that, you will see a message on the server in-game, that you can accept the verification typing /discord accept or reject it by /discord deny command.
If you accepted it and did everything right, roles set in config.yml should be assigned to you.

## Permissions
- *discordverifier.discord* - use the /discord (accept/deny) command;
- *discordverifier.admin* - use /discord info and /discord reload commands.
