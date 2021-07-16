package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.server.Node
import xyz.missingnoshiny.ftg.server.nodes
import java.net.InetAddress
import java.time.Duration

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
            val remoteHost = this.call.request.origin.remoteHost
            println("Host: $remoteHost")
            kotlin.runCatching {
                val address = InetAddress.getByName(remoteHost)
                println("Address: ${address.hostAddress}")
            }

            val node = Node(this)
            nodes += node
            node.handler.handleIncomingEvents()
            nodes -= node
        }
    }
}

