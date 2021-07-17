package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.core.events.WebsocketSessionEventHandler
import xyz.missingnoshiny.ftg.server.Node
import xyz.missingnoshiny.ftg.server.events.NodeHeartbeatEvent
import xyz.missingnoshiny.ftg.server.events.NodeReadyEvent
import xyz.missingnoshiny.ftg.server.events.NodeServerEventContext
import xyz.missingnoshiny.ftg.server.nodes
import java.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.configureSockets() {

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") {
            println("New websocket connection!")
            val node = Node()

            // If the NodeReadyEvent isn't received within 5 seconds, assume a problem occurred or the client isn't a Node
            launch {
                delay(kotlin.time.Duration.seconds(5))
                if (!node.isReady) close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Not a Node"))
            }

            val handler = WebsocketSessionEventHandler(NodeServerEventContext(node), this)
            handler.registerIncomingEvent(NodeReadyEvent::class)
            handler.registerIncomingEvent(NodeHeartbeatEvent::class)

            nodes += node
            handler.handleIncomingEvents()  // Blocking until connection is closed
            nodes -= node
        }
    }
}

