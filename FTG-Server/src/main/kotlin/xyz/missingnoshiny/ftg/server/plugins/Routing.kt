package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import xyz.missingnoshiny.ftg.server.nodes

fun Application.configureRouting() {

    routing {
        get("/nodes") {
            call.respond(nodes.filter { it.isReady })
        }
    }
}
