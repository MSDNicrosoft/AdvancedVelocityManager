package work.msdnicrosoft.avm.command.whitelist

import work.msdnicrosoft.avm.module.whitelist.WhitelistManager
import work.msdnicrosoft.avm.util.command.builder.*
import work.msdnicrosoft.avm.util.command.context.CommandContext
import work.msdnicrosoft.avm.util.component.builder.minimessage.tag.tr
import work.msdnicrosoft.avm.util.server.task
import work.msdnicrosoft.avm.util.string.isUuid
import work.msdnicrosoft.avm.util.string.toUuid

object NoteCommand {
    val command = literalCommand("note") {
        requires { hasPermission("avm.command.whitelist.note") }
        literal("set") {
            wordArgument("player") {
                suggests { builder ->
                    WhitelistManager.usernames.forEach(builder::suggest)
                    builder.buildFuture()
                }
                wordArgument("key") {
                    suggests { builder ->
                        val player: String by this
                        val entry = if (player.isUuid()) {
                            WhitelistManager.getPlayer(player.toUuid())
                        } else {
                            WhitelistManager.getPlayer(player)
                        }
                        entry?.extra?.keys?.forEach(builder::suggest)
                        builder.buildFuture()
                    }
                    greedyStringArgument("value") {
                        executes {
                            val player: String by this
                            val key: String by this
                            val value: String by this
                            task { setNote(player, key, value) }
                            Command.SINGLE_SUCCESS
                        }
                        literal("view") {
                            wordArgument("player") {
                                suggests { builder ->
                                    WhitelistManager.usernames.forEach(builder::suggest)
                                    builder.buildFuture()
                                }
                                executes {
                                    val player: String by this
                                    task { viewNote(player) }
                                    Command.SINGLE_SUCCESS
                                }
                            }
                        }
                    }
                }
            }
        }
        literal("remove") {
            wordArgument("player") {
                suggests { builder ->
                    WhitelistManager.usernames.forEach(builder::suggest)
                    builder.buildFuture()
                }
                wordArgument("key") {
                    suggests { builder ->
                        val player: String by this
                        val entry = if (player.isUuid()) {
                            WhitelistManager.getPlayer(player.toUuid())
                        } else {
                            WhitelistManager.getPlayer(player)
                        }
                        entry?.extra?.keys?.forEach(builder::suggest)
                        builder.buildFuture()
                    }
                    executes {
                        val player: String by this
                        val key: String by this
                        task { removeNote(player, key) }
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

    private fun CommandContext.setNote(player: String, key: String, value: String) {
        val isUuid: Boolean = player.isUuid()
        val success: Boolean = if (isUuid) {
            WhitelistManager.setNote(player.toUuid(), key, value)
        } else {
            WhitelistManager.setNote(player, key, value)
        }

        if (success) {
            sendMessage(
                tr("avm.command.avmwl.note.set.success") {
                    args {
                        string("player", player)
                        string("key", key)
                        string("value", value)
                    }
                }
            )
        } else {
            sendTranslatable("avm.command.avmwl.note.player_not_found")
        }
    }

    private fun CommandContext.removeNote(player: String, key: String) {
        val isUuid: Boolean = player.isUuid()
        val success: Boolean = if (isUuid) {
            WhitelistManager.removeNote(player.toUuid(), key)
        } else {
            WhitelistManager.removeNote(player, key)
        }

        if (success) {
            sendMessage(
                tr("avm.command.avmwl.note.remove.success") {
                    args {
                        string("player", player)
                        string("key", key)
                    }
                }
            )
        } else {
            sendTranslatable("avm.command.avmwl.note.remove.not_found")
        }
    }

    private fun CommandContext.viewNote(player: String) {
        val isUuid: Boolean = player.isUuid()
        val entry = if (isUuid) {
            WhitelistManager.getPlayer(player.toUuid())
        } else {
            WhitelistManager.getPlayer(player)
        }

        if (entry == null) {
            sendTranslatable("avm.command.avmwl.note.player_not_found")
            return
        }

        sendMessage(tr("avm.command.avmwl.note.view.header") { args { string("player", entry.name) } })

        if (entry.extra.isEmpty()) {
            sendTranslatable("avm.command.avmwl.note.view.empty")
            return
        }

        entry.extra.forEach { (key, value) ->
            sendMessage(
                tr("avm.command.avmwl.note.view.entry") {
                    args {
                        string("key", key)
                        string("value", value)
                    }
                }
            )
        }
    }
}
