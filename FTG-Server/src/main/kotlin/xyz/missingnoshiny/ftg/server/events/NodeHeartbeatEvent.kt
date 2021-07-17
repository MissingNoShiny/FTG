package xyz.missingnoshiny.ftg.server.events

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent

@Serializable
data class NodeHeartbeatEvent(override val timestamp: Instant, val roomCount: Int) : IncomingEvent() {
    override fun invoke(context: EventContext) {
        (context as NodeServerEventContext).let {
            it.node.roomCount = roomCount
            // Use server time
            it.node.lastHeartBeat = Clock.System.now()
        }
    }
}