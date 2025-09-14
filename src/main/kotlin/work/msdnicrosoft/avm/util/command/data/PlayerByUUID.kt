package work.msdnicrosoft.avm.util.command.data

import com.velocitypowered.api.proxy.Player
import java.util.UUID

data class PlayerByUUID(val uuid: UUID, val player: Player)
