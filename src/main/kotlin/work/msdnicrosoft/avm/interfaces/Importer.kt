package work.msdnicrosoft.avm.interfaces

import taboolib.common.platform.ProxyCommandSender

interface Importer {

    val pluginName: String

    fun ProxyCommandSender.import(defaultServer: String): Boolean
}
