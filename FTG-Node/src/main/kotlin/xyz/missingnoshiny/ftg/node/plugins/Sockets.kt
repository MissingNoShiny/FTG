package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import xyz.missingnoshiny.ftg.node.Player
import xyz.missingnoshiny.ftg.node.rooms
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/room/{id}") {
            val id = call.parameters["id"]!!
            if (id !in rooms) return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid room id"))
            val room = rooms[id]!!
            room.players.add(Player(this))
        }
    }
}


