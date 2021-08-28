package xyz.missingnoshiny.ftg.server.events

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent
import xyz.missingnoshiny.ftg.server.rooms

@Serializable
class NodeRemoveRoomEvent(override val timestamp: Instant, val id: String): IncomingEvent() {
    override suspend fun invoke(context: EventContext) {
        rooms.remove(id)
    }

}