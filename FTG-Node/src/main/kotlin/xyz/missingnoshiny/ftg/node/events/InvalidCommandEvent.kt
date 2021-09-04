package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class InvalidCommandEvent(val errorMessage: String): OutgoingEvent() {
    companion object {
        val NO_PERMISSION = InvalidCommandEvent("Vous ne pouvez pas utiliser cette commande")
        val INVALID = InvalidCommandEvent("Commande invalide")
    }
}