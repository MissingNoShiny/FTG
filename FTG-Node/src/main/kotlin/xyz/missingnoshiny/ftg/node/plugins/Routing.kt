package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.rooms

fun Application.configureRouting() {

    routing {
        get("/")   {
            call.respondText("Node")
        }
        post("/createRoom/{id}") {
            val id = call.parameters["id"]!!
            if (id in rooms) call.respond(HttpStatusCode.InternalServerError)
            val room = Room(id)
            rooms[id] = room
            launch {
                room.activityCheckLoop()
            }
            call.respond(HttpStatusCode.Created)
        }
    }
}
