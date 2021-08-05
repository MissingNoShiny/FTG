package xyz.missingnoshiny.ftg.server.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.missingnoshiny.ftg.server.api.CreateRoomResponse
import xyz.missingnoshiny.ftg.server.api.UserInfo
import xyz.missingnoshiny.ftg.server.db.ExternalUser
import xyz.missingnoshiny.ftg.server.db.LocalUser
import xyz.missingnoshiny.ftg.server.db.User
import xyz.missingnoshiny.ftg.server.db.Users
import xyz.missingnoshiny.ftg.server.getLeastUsed
import xyz.missingnoshiny.ftg.server.nodes
import xyz.missingnoshiny.ftg.server.rooms

fun Application.configureRouting() {

    val client = HttpClient()

    routing {

        get("/ping") {
            call.respond("Pong")
        }

        get("/nodes") {
            call.respond(nodes.filter { it.isReady })
        }
        post("/createRoom") {

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


        route("/userinfo") {

            fun getUserInfos(user: User): UserInfo = when (user.type) {
                Users.Type.LOCAL -> {
                    val localUser = transaction {
                        LocalUser[user.id]
                    }
                    UserInfo(localUser.username, user.type)
                }
                Users.Type.EXTERNAL -> {
                    val externalUser = transaction {
                        ExternalUser[user.id]
                    }
                    UserInfo(externalUser.username, user.type)
                }
            }


            // Get info of authenticated user
            authenticate("auth-jwt") {
                get {
                    println("self")
                    val principal = call.principal<JWTPrincipal>()
                    val id = principal!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val user = transaction {
                        User.findById(id)
                    } ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(HttpStatusCode.OK, getUserInfos(user))
                }
            }

            // Get info of any user, does not require authentication
            get("/{id}") {
                println("id")
                val user = transaction {
                    User.findById(call.parameters["id"]!!.toInt())
                } ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.OK, getUserInfos(user))
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
