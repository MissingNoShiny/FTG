package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.core.CreateRoomBody
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.rooms

fun Application.configureRouting() {

    routing {
        get("/")   {
            call.respondText("Node")
        }
        post("/createRoom/{id}") {
            val id = call.parameters["id"]!!
            val isPublic = call.receiveOrNull<CreateRoomBody>()?.isPublic ?: return@post call.respond(HttpStatusCode.BadRequest)
            if (id in rooms) call.respond(HttpStatusCode.InternalServerError)
            val room = Room(id, isPublic)
            rooms[id] = room
            launch {
                room.activityCheckLoop()
            }
            call.respond(HttpStatusCode.Created)
        }
        authenticate("auth-jwt") {
            get("/has-room/{id}") {
                println(rooms.map { it.key })
                val id = call.parameters["id"]!!
                println(id)
                call.respond(if (id in rooms) {
                    HttpStatusCode.OK
                } else {
                    HttpStatusCode.NotFound
                })
            }
        }
    }
}
