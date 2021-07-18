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
        get("/createRoom/{id}") {
            val id = call.parameters["id"]!!
            if (id in rooms) call.respond(HttpStatusCode.InternalServerError)
            rooms[id] = Room()
            call.respond(HttpStatusCode.Created)
        }
    }
}
