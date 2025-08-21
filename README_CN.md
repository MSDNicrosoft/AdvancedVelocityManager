# AdvancedVelocityManager

<div align="right">
  <a title="English" href="./README.md" >English</a>
  |
  简体中文
</div>

## 简介

AdvancedVelocityManager 是一个为 Minecraft Velocity 代理服务器设计的高级管理插件。<br>
它提供了一整套强大的工具，帮助服务器管理员更高效地进行玩家管理和服务器自动化操作。<br>
该插件支持玩家在不同服务器间的快速传送、自定义广播消息、与 [Floodgate](https://geysermc.org/wiki/floodgate/) 兼容的精细化白名单管理，以及增强的跨服聊天体验。

## 功能亮点

- **跨服务器发送 (`/avm send` 和 `/avm sendall`)**：将单个或批量玩家从一个服务器发送到另一个服务器，并可选择提供理由。
- **玩家踢出** (`/avm kick` 和 `/avm kickall`): 将单个或所有玩家踢出服务器，并可选择提供理由。
- **与 [Floodgate](https://geysermc.org/wiki/floodgate/) 兼容的白名单管理 (`/avmwl`)**：通过 UUID 和用户名添加/移除玩家白名单，并能够为每个玩家分配特定服务器或服务器组的访问权限。
- **自定义广播**：自定义玩家加入、离开和服务器切换的广播消息。
- **Tab 列表同步**: 实现跨服的 Tab 列表显示一致性，同时支持自定义显示格式。
- **跨服聊天（Chat-Bridge）**: 允许不同服务器间的玩家进行聊天，同时支持自定义聊天格式。

## 安装指南

1. 下载 AdvancedVelocityManager 插件的最新版本。
2. 将插件文件放入 Velocity 服务器的 `plugins` 目录中。
3. 重启 Velocity 服务器以加载插件。
4. 根据需要编辑 `config.yml` 文件以调整插件设置，然后执行命令 `/avm reload` 以重载插件。

## 使用方法

- **白名单管理**：使用 `/avmwl ...` 命令
- **单个玩家跨服务器发送**：使用 `/avm send <玩家名> <目标服务器>` 命令。
- **批量跨服务器发送**：使用 `/avm sendall <源服务器> <目标服务器>` 命令。
- **玩家踢出**：使用 `/avm kick <玩家名> [理由]` 命令。
- **批量踢出**：使用 `/avm kickall <服务器> [理由]` 命令从指定服务器踢出所有玩家。

### 权限

- `avm.command.info` - 查看插件信息
- `avm.command.reload` - 放弃内存中的数据，并从文件中重载配置、语言和白名单
- `avm.command.confirm` - 确认操作
- `avm.command.import` - 从其它插件（[lls-manager](https://github.com/plusls/lls-manager) [VelocityWhitelist](https://gitee.com/virtual-qu-an/velocity-whitelist)）导入数据
- `avm.command.kick` - 踢出指定玩家
- `avm.command.send` - 将单个玩家从一个服务器发送到另一个服务器
- `avm.command.sendall` - 将指定服务器的所有玩家发送到另一个服务器
- `avm.command.kickall` - 从指定服务器踢出所有玩家
- `avm.command.whitelist.list` - 查看白名单
- `avm.command.whitelist.add` - 通过用户名或 UUID 将玩家添加到白名单
- `avm.command.whitelist.remove` - 从白名单中移除玩家
- `avm.command.whitelist.clear` - 清空白名单
- `avm.command.whitelist.find` - 通过关键词在白名单内查找玩家
- `avm.command.whitelist.on` - 开启白名单
- `avm.command.whitelist.off` - 关闭白名单
- `avm.command.whitelist.status` - 查看白名单状态
- `avm.sendall.bypass` - 绕过 `/sendall` 命令
- `avm.kickall.bypass` - 绕过 `/kickall` 命令

### 配置文件

配置文件 `config.yml` 允许您自定义插件的各个方面，包括服务器映射、广播消息、命令配置、白名单设置等。

## 致谢

本插件的开发受到其他开源项目的启发，包括但不限与 [cancellable-chat](https://github.com/ZhuRuoLing/cancellable-chat) 和 [lls-manager](https://github.com/plusls/lls-manager)。<br>
我们感谢这些项目对开源社区的贡献。

## 支持与社区

- **问题反馈**：创建 GitHub Issue 报告任何问题或功能请求。

## TODO

- [ ] 发布到 [Modrinth](https://modrinth.com)
- [x] `/msg` 跨服私聊
- [x] TabList 同步
- [ ] 更高级的聊天交互
- [x] 从 lls-manager 导入数据
- [x] 离线模式白名单
- [ ] Web 接口请求管理 (HTTP / gRPC / WebSocket) **[可能废弃]**
- [x] 聊天消息日志输出

## 许可与版权

AdvancedVelocityManager 根据 MIT 许可证 发布。您可以自由使用、修改和分发此插件，但请保留原作者的版权声明。

## 贡献

我们欢迎对 AdvancedVelocityManager 的贡献。无论是通过代码、错误报告或功能建议，您的输入对我们都很有价值。
