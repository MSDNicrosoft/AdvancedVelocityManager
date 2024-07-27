<div style="text-align: right">
  <a title="English" href="./README.md" >English</a>
  |
  简体中文
</div>

# AdvancedVelocityManager

## 简介

AdvancedVelocityManager 是一款为 Velocity 端设计的 Minecraft 服务器管理插件，<br>
它提供了全面的管理功能，包括跨服务器发送玩家、踢出玩家、与 [Floodgate](https://geysermc.org/wiki/floodgate/) 兼容的白名单管理（确保多平台玩家的无缝体验）等。

## 功能亮点

- **跨服务器发送 (`/send`)**：将单个玩家从一个服务器发送到另一个服务器。
- **批量跨服务器发送 (`/sendall`)**：将指定服务器的所有玩家发送到另一个服务器。
- **玩家踢出 (`/kick`)**：允许管理员踢出指定玩家，并可选择提供理由。
- **批量踢出 (`/kickall`)**：允许管理员从指定服务器踢出所有玩家，并可选择提供理由。
- **白名单管理 (`/avmwl`)**：支持通过 UUID 和用户名添加玩家到白名单，并**与 [Floodgate](https://geysermc.org/wiki/floodgate/) 兼容**。
- **自定义广播**：自定义玩家加入、离开和服务器切换的广播消息。

## 安装指南

1. 下载 AdvancedVelocityManager 插件的最新版本。
2. 将插件文件放入 Velocity 服务器的 `plugins` 文件夹中。
3. 根据需要编辑 `config.yml` 文件以调整插件设置。
4. 重启 Velocity 服务器以加载插件。

## 使用方法

- **白名单管理**: 使用 `/avmwl ...` 命令
- **单个玩家跨服务器发送**：使用 `/send <玩家名> <目标服务器>` 命令。
- **批量跨服务器发送**：使用 `/sendall <源服务器> <目标服务器>` 命令。
- **玩家踢出**：使用 `/kick <玩家名> [理由]` 命令。
- **批量踢出**：使用 `/kickall <服务器> [理由]` 命令从指定服务器踢出所有玩家。

## 配置文件

配置文件 `config.yml` 允许您自定义插件的各个方面，包括服务器映射、广播消息、命令配置、白名单设置等。

## 致谢

AdvancedVelocityManager 的开发受到其他开源项目如 [cancellable-chat](https://github.com/ZhuRuoLing/cancellable-chat)、[lls-manager](https://github.com/plusls/lls-manager)、[artifex](https://github.com/InsinuateProjects/artifex) 和 [TrMenu](https://github.com/TrPlugins/TrMenu) 的启发，<br>
并整合了这些项目的元素。我们感谢这些项目对开源社区的贡献。

## 支持与社区

- **问题反馈**：创建 GitHub Issue 报告任何问题或功能请求。

## TODO

- [ ] 发布到 [Modrinth](https://modrinth.com)
- [ ] `/msg` 跨服私聊
- [ ] `/tp` 跨服传送
- [ ] TabList 同步
- [ ] 更高级的聊天交互

## 许可与版权

AdvancedVelocityManager 根据 MIT 许可证 发布。您可以自由使用、修改和分发此插件，但请保留原作者的版权声明。

## 贡献

我们欢迎对 AdvancedVelocityManager 的贡献。无论是通过代码、错误报告或功能建议，您的输入对我们都很有价值。
