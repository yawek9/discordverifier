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

## Commands
- */discord reload* - reloads plugin configuration;
- */discord info (nickname)* - shows information about verified player;
- */discord accept/deny* - accepts/denies verification request;
- */discord unlink* - cancels verification and unlinks Discord account.

## Permissions
- *discordverifier.discord* - use the /discord command;
- *discordverifier.reload* - use the /discord reload command;
- *discordverifier.info* - use the /discord info command;
- *discordverifier.accept* - use the /discord accept command;
- *discordverifier.deny* - use the /discord deny command;
- *discordverifier.unlink* - use the /discord unlink command.
