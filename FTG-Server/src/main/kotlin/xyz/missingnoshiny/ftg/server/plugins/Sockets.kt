package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.*
import io.ktor.network.tls.*
import io.ktor.utils.io.core.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import kotlinx.coroutines.awaitAll
import xyz.missingnoshiny.ftg.server.Node
import xyz.missingnoshiny.ftg.server.nodes

fun Application.configureSockets() {


    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") { // websocketSession
            println("Connexion !!")
            val session = this
            val node = Node(session)
            nodes += node
            node.handler.handleIncomingEvents()
            nodes -= node
        }
    }
}

