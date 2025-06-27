# AdvancedVelocityManager

<div align="right">
  English
  |
  <a title="简体中文" href="./README_CN.md" >简体中文</a>
</div>

## Introduction

AdvancedVelocityManager is an advanced management plugin designed for the Minecraft Velocity proxy server.<br>
It provides a comprehensive set of powerful tools to help server administrators manage players and automate server operations more efficiently.<br>
This plugin supports fast transfer of players between different servers, custom broadcast messages, fine-grained whitelist management compatible with [Floodgate](https://geysermc.org/wiki/floodgate/), and enhanced cross-server chat experience.

## Features

- **Cross-Server Send (`/avm send` and `/avm sendall`)**: Send a single or batch of players from one server to another, with the option to provide a reason.
- **Player Kick (`/avm kick` and `/avm kickall`)**: Kick a single player or all players from the server, with the option to provide a reason.
- **[Floodgate](https://geysermc.org/wiki/floodgate/) Compatible Whitelist Management (`/avmwl`)**: Add/Remove players from the whitelist by UUID and username, and assign specific server or server group access rights to each player.
- **Custom Broadcasts**: Customize broadcast messages for player joins, leaves, and server switches.
- **Tab List Synchronization**: Achieve consistency in cross-server Tab list display, with support for custom display formats.
- **Cross-Server Chat (Chat-Bridge)**: Allows players from different servers to chat, with support for custom chat formats.

## Installation Guide

1. Download the latest version of the AdvancedVelocityManager plugin.
2. Place the plugin file into the `plugins` directory of your Velocity server.
3. Restart the Velocity server to load the plugin.
4. Edit the `config.yml` file to adjust plugin settings as needed, then execute the command `/avm reload` to reload the plugin.

## Usage

- **Whitelist Management**: Use the `/avmwl ...` command.
- **Single Player Cross-Server Send**: Use the command `/avm send <playername> <targetserver>`.
- **Batch Cross-Server Send**: Use the command `/avm sendall <sourceserver> <targetserver>`.
- **Player Kick**: Use the command `/avm kick <playername> [reason]`.
- **Batch Kick**: Use the command `/avm kickall <server> [reason]` to kick all players from a specified server.

### Permissions

- `avm.command.info` - View plugin information
- `avm.command.reload` - Discard data in memory, then reload configuration, language and whitelist from files
- `avm.command.confirm` - Confirm actions
- `avm.command.import` - Import data from other plugins ([lls-manager](https://github.com/plusls/lls-manager) [VelocityWhitelist](https://gitee.com/virtual-qu-an/velocity-whitelist))
- `avm.command.kick` - Kick a specified player
- `avm.command.send` - Send a specified player from one server to another
- `avm.command.sendall` - Send all players from a specified server to another
- `avm.command.kickall` - Kick all players from a specified server
- `avm.command.whitelist.list` - View the whitelist
- `avm.command.whitelist.add` - Add a player to the whitelist by username or UUID
- `avm.command.whitelist.remove` - Remove a player from the whitelist
- `avm.command.whitelist.clear` - Clear the whitelist
- `avm.command.whitelist.find` - Find player(s) within the whitelist by keyword
- `avm.command.whitelist.on` - Turn on the whitelist
- `avm.command.whitelist.off` - Turn off the whitelist
- `avm.command.whitelist.status` - View whitelist status
- `avm.sendall.bypass` - Bypass `/sendall` command
- `avm.kickall.bypass` - Bypass `/kickall` command

### Configuration File

The `config.yml` configuration file allows you to customize various aspects of the plugin, including server mapping, broadcast messages, command configurations, whitelist settings and etc.

## Acknowledgements

The development of this plugin was inspired by other open-source projects, including but not limited to [cancellable-chat](https://github.com/ZhuRuoLing/cancellable-chat), [lls-manager](https://github.com/plusls/lls-manager), [artifex](https://github.com/InsinuateProjects/artifex), and [TrMenu](https://github.com/TrPlugins/TrMenu) .<br>
We appreciate the contributions of these projects to the open-source community.

## Support and Community

- **Issue Reporting**: For any issues or feature requests, use the GitHub issue tracker.

## TODO

- [ ] Publish to [Modrinth](https://modrinth.com) & [SpigotMC](https://www.spigotmc.org/)
- [x] `/msg` Private chat across servers
- [x] TabList Synchronization
- [ ] More advanced chat interaction
- [x] Import data from lls-manager
- [x] Offline whitelist support
- [ ] Web Interface Request Management (HTTP / gRPC / WebSocket)  **[May be deprecated]**
- [ ] Chat Message logging
- [ ] Multi-language support

## License and Copyright

AdvancedVelocityManager is released under the MIT License. You are free to use, modify, and distribute this plugin,
provided you retain the original author's copyright notice.

## Contribution

We welcome contributions to AdvancedVelocityManager. Whether it's through code, bug reports, or feature suggestions,
your input is valuable to us.
