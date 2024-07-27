# AdvancedVelocityManager

<div align="right">
  English
  |
  <a title="简体中文" href="./README_CN.md" >简体中文</a>
</div>

## Introduction

AdvancedVelocityManager is a comprehensive management plugin designed for the Velocity proxy server in Minecraft,<br>
offering a suite of powerful tools to make server administration more efficient and automated.<br>
It supports features such as sending players between servers, custom player broadcasts and<br>
whitelist management which is compatible with the [Floodgate](https://geysermc.org/wiki/floodgate/) plugin for a seamless cross-platform player experience.

## Key Features

- **Server-to-Server Send (`/send`)**: Send individual players from one server to another.
- **Bulk Server-to-Server Send (`/sendall`)**: Send all players from a specified server to another.
- **Player Kick (`/kick`)**: Allows admins to kick specified players with an optional reason.
- **Bulk Kick (`/kickall`)**: Allows admins to kick all players from a specified server with an optional reason.
- **Whitelist Management (`/avmwl`)**: Add players to the whitelist using UUIDs or usernames, and compatible with
  [Floodgate](https://geysermc.org/wiki/floodgate/)
- **Custom Broadcasts**: Customize messages for player joins, leaves, and server switches.

## Installation Guide

1. Download the latest version of the AdvancedVelocityManager plugin.
2. Place the plugin file in the `plugins` directory of your Velocity server.
3. Edit the `config.yml` file to adjust the plugin settings as needed.
4. Restart the Velocity server to load the plugin.

## Usage

- **Whitelist Management**: Use the `/avmwl ...` command.
- **Individual Send Between Servers**: Use the `/send <player> <target server>` command.
- **Bulk Send Between Servers**: Use the `/sendall <source server> <target server>` command.
- **Player Kick**: Use the `/kick <player> [reason]` command.
- **Bulk Kick**: Use the `/kickall <server> [reason]` command to kick all players from a specified server.

## Configuration File

The `config.yml` file allows you to customize various aspects of the plugin, including server mapping, broadcast
messages, command configurations, whitelist settings, etc.

## Acknowledgements

The development of AdvancedVelocityManager has been inspired by and has incorporated elements from other open-source
projects such as [cancellable-chat](https://github.com/ZhuRuoLing/cancellable-chat), [lls-manager](https://github.com/plusls/lls-manager), [artifex](https://github.com/InsinuateProjects/artifex), and [TrMenu](https://github.com/TrPlugins/TrMenu) .<br>
We are grateful for the contributions of these projects to the open-source community.

## Support and Community

- **Issue Reporting**: For any issues or feature requests, use the GitHub issue tracker.

## TODO

- [ ] Publish to [Modrinth](https://modrinth.com) 
- [ ] `/msg` Private chat across servers
- [ ] `/tp` Teleport across servers
- [ ] TabList Synchronization
- [ ] More advanced chat interaction

## License and Copyright

AdvancedVelocityManager is released under the MIT License. You are free to use, modify, and distribute this plugin,
provided you retain the original author's copyright notice.

## Contribution

We welcome contributions to AdvancedVelocityManager. Whether it's through code, bug reports, or feature suggestions,
your input is valuable to us.
