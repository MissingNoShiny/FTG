package xyz.missingnoshiny.ftg.server.events

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.RoomSerializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent
import xyz.missingnoshiny.ftg.server.nodes

@Serializable
data class NodeHeartbeatEvent(override val timestamp: Instant, val rooms: MutableMap<String, RoomSerializable>) : IncomingEvent() {
    override suspend fun invoke(context: EventContext) {
        (context as NodeServerEventContext).let {
            it.node.rooms = rooms
            // Use server time
            it.node.lastHeartBeat = Clock.System.now()
        }
    }
}