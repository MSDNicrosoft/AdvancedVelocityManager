package work.msdnicrosoft.avm.command

import com.sksamuel.hoplite.fp.valid

object CommandType {

    enum class WhitelistType(command: String) {
        ADD("add"),
        REMOVE("remove"),
        FIND("find"),
        LIST("list"),
        ON("on"),
        OFF("off"),
        STATUS("status"),
        CLEAR("clear")
    }
}