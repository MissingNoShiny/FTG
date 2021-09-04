package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.missingnoshiny.ftg.core.CreateRoomBody
import xyz.missingnoshiny.ftg.core.RoomSerializable
import xyz.missingnoshiny.ftg.server.api.CreateRoomResponse
import xyz.missingnoshiny.ftg.server.db.*
import xyz.missingnoshiny.ftg.server.getLeastUsed
import xyz.missingnoshiny.ftg.server.nodes
import xyz.missingnoshiny.ftg.server.rooms

fun Application.configureRouting() {

    val client = HttpClient() {
        install(JsonFeature)
    }

    routing {

        get("/ping") {
            call.respond("Pong")
        }

        get("/nodes") {
            call.respond(nodes.filter { it.isReady })
        }

        get("/room/{id}") {
            val node = rooms[call.parameters["id"]] ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(node)
        }


        route("/userinfo") {

            // Get info of authenticated user
            authenticate("auth-jwt") {
                get {
                    println("self")
                    val principal = call.principal<JWTPrincipal>()
                    val id = principal!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val user = transaction {
                        User.findById(id)
                    } ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(HttpStatusCode.OK, UserService.getUserInfos(user))
                }
            }

            // Get info of any user, does not require authentication
            get("/{id}") {
                println("id")
                val user = transaction {
                    User.findById(call.parameters["id"]!!.toInt())
                } ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.OK, UserService.getUserInfos(user))
            }
        }

        authenticate("auth-jwt") {
            post("/createRoom") {
                val requestBody = call.receiveOrNull<CreateRoomBody>() ?: return@post call.respond(HttpStatusCode.BadRequest)

                val node = nodes.getLeastUsed() ?: return@post call.respond(HttpStatusCode.InternalServerError)

                var id = generateRoomId()
                while (id in rooms) id = generateRoomId()

                println(id)

                val response: HttpResponse = client.post("https://${node.apiAddress}/createRoom/$id") {
                    contentType(ContentType.Application.Json)
                    body = requestBody
                }
                if (response.status != HttpStatusCode.Created) return@post call.respond(HttpStatusCode.InternalServerError)
                rooms[id] = node
                call.respond(HttpStatusCode.Created, CreateRoomResponse(id))
            }

            get("/publicRooms") {
                val publicRooms = mutableListOf<RoomSerializable>()
                rooms.forEach { (id, node) ->
                    val room = node.rooms[id]
                    if (room != null && room.isPublic) publicRooms.add(room)
                }

                call.respond(HttpStatusCode.OK, publicRooms)
            }

            get("/follow/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val id = principal!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)

            }

            get("/unfollow/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val id = principal!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

/**
 * Generates a random alphanumeric room ID
 */
fun generateRoomId(): String {
    val allowedCharacters = ('A'..'Z') + ('0'..'9')
    return List(4) { allowedCharacters.random() }.joinToString("")
}
