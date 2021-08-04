package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.server.getLeastUsed
import xyz.missingnoshiny.ftg.server.nodes
import xyz.missingnoshiny.ftg.server.rooms

fun Application.configureRouting() {

    val client = HttpClient()

    routing {
        get("/nodes") {
            call.respond(nodes.filter { it.isReady })
        }
        post("/createRoom") {
            @Serializable
            data class CreateRoomResponse(val id: String)

            val node = nodes.getLeastUsed() ?: return@post call.respond(HttpStatusCode.InternalServerError)

            var id = generateRoomId()
            while (id in rooms) id = generateRoomId()

            println(id)

            val response: HttpResponse = client.request("${node.apiAddress}/createRoom/$id") {
                method = HttpMethod.Post
            }
            if (response.status != HttpStatusCode.Created) return@post call.respond(HttpStatusCode.InternalServerError)
            call.respond(HttpStatusCode.Created, CreateRoomResponse(id))
        }
        get("/room/{id}") {
            val node = rooms[call.parameters["id"]] ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(node)
        }
    }
}

fun generateRoomId(): String {
    val allowedCharacters = ('A'..'Z') + ('0'..'9')
    return List(4) { allowedCharacters.random() }.joinToString("")
}
