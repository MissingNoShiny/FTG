package xyz.missingnoshiny.ftg.server.events

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent

@Serializable
data class NodeReadyEvent(override val timestamp: Instant, val apiAddress: String) : IncomingEvent() {
    override suspend fun invoke(context: EventContext) {
        (context as NodeServerEventContext).node.apiAddress = apiAddress
    }
}