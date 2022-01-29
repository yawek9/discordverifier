# DiscordVerifier

Simple Velocity server proxy plugin that links server groups to the Discord roles. Inspired by https://github.com/StaticFX/DiscordBotBungee.

# Requirements
- Java 17
- Discord bot with enabled intents

# Config

After first startup, you have to stop the server and fill generated config with correct data. 

Main things thay you have to change in the config.yml file:
```
# Set this to true, if you shall to use MySQL database
UseMySQL: false
# Set here your database address
DatabaseAddress: "localhost"
# Set here your database port
DatabasePort: "3306"
# Set here your database name
DatabaseName: "discordbot"
# Set here your database user
DatabaseUser: "root"
# Set here your database password
DatabasePassword: ""
# Set here your bot token (read more https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token)
BotToken: "ENTER_YOUR_BOT_TOKEN_HERE"
# Set here your Discord server ID (read more https://support.discord.com/hc/pl/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-)
GuildID: "ENTER_YOUR_GUILD_ID_HERE"
# Set here your channel ID, that will be used for verification command(s?) (more ^^)
VerificationChannelID: "ENTER_YOUR_VERIFICATION_CHANNEL_ID_HERE"
# The time in seconds after which the bot will delete all messages (the useful ones, other are deleted immediately)
DeleteMessageAfter: 10
# The time in seconds after which the verification will expire
VerificationExpireAfter: 120
# If only one, the highest placed role should be assigned to the user
LimitRolesToOne: true
# If true, users names on Discord will be synchronized with nicknames on the server
ForceNicknamesOnDiscord: true
[...]
# Here set groups with IDs of roles that should be added to the user, permission based, as below
Roles:
  admin: "111111111111111111" # If player has permission group.admin, he will get Discord role with ID 111111111111111111
  vip: "222222222222222222" # If player has permission group.vip, he will get Discord role with ID 222222222222222222
  default: "333333333333333333" # If player has permission group.default, he will get Discord role with ID 333333333333333333
```
Other of them are based on your preferences. There are used custom hex based color codes used in chat messages.

Colors
```
Single color: &#(hex code)(text)
Two-color gradient: &#gr(hex code)(hex code)(text)
Rainbow: &#rb(text)
```

Styles
```
Obfuscated: &k(text)
Bold: &l(text)
Strikethrough: &m(text)
Underlined: &n(text)
Italic: &o(text)
```

# Usage

Permission for the player commands is 'discordverifier.discord'. Also for the admins ones 'discordverifier.admin'. 
After setting everything up and enabling the servers, if all is configured well, you can try to get verified. 
To do this, enter the server and type in verify channel "!verify (your nickname)". 
After that, you will see a message on the server in game, that you can accept verification typing /discord accept or reject it by /discord deny command.
If you accepted it and did everything right, roles set in config.yml should be assigned to you.

# Help

If you want to get some help or report bugs, join the Discord server: https://discord.gg/cpY2Qcjjcz.
