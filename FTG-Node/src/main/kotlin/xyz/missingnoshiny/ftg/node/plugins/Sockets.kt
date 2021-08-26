package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import xyz.missingnoshiny.ftg.node.games.User
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
        authenticate("auth-jwt") {
            webSocket("/room/{id}") {
                val principal = call.principal<JWTPrincipal>()!!

                val id = call.parameters["id"]!!
                if (id !in rooms) return@webSocket this.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid room id"))
                val room = rooms[id]!!
                val user = User(principal.subject!!.toInt(), principal.payload.getClaim("username").asString(), room, this)

                room.addUser(user)
                user.handler.handleIncomingEvents()
                room.removeUser(user)
            }
        }
    }
}


