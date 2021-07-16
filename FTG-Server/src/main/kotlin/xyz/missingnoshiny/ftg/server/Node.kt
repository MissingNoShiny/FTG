package xyz.missingnoshiny.ftg.server

import io.ktor.http.cio.websocket.*
import xyz.missingnoshiny.ftg.core.events.WebsocketSessionEventHandler
import xyz.missingnoshiny.ftg.server.events.NodeHeartbeatEvent
import xyz.missingnoshiny.ftg.server.events.NodeServerEventContext

class Node(session: DefaultWebSocketSession) {
    val handler = WebsocketSessionEventHandler(NodeServerEventContext(this), session)

    init {
        handler.registerIncomingEvent(NodeHeartbeatEvent::class)
    }

    var roomCount = 0

    val weight: Int
        get() = roomCount
}