package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import xyz.missingnoshiny.ftg.node.Room
import xyz.missingnoshiny.ftg.node.rooms

fun Application.configureRouting() {

    routing {
        get("/")   {
            call.respondText("Node")
        }
        get("/create") {
            val room = Room()
            println(room.id)
            rooms.add(room)
            call.respond(HttpStatusCode.Created)
        }
    }
}
