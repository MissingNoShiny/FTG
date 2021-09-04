package xyz.missingnoshiny.ftg.server.plugins.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import xyz.missingnoshiny.ftg.server.api.AuthorizationCodeBody
import xyz.missingnoshiny.ftg.server.api.SignupRequest
import xyz.missingnoshiny.ftg.server.api.UserLogin
import xyz.missingnoshiny.ftg.server.db.*
import java.time.Instant
import java.util.*
import javax.print.attribute.standard.JobOriginatingUserName

fun Application.configureAuthentication() {

    /**
     * JWT config
     */
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val duration = environment.config.property("jwt.duration_ms").getString().toInt()

    fun generateJWTToken(id: Int, username:String, administrator: Boolean) = JWT.create()
        .withAudience(audience)
        .withSubject(id.toString())
        .withClaim("username", username)
        .withClaim("administrator", administrator)
        .withExpiresAt(Date(System.currentTimeMillis() + duration))
        .sign(Algorithm.HMAC256(secret))

    fun ResponseCookies.addJWTToken(id: Int, username: String, administrator: Boolean) {
        val token = generateJWTToken(id, username, administrator)
        append("token", token, httpOnly = true, secure = true, domain = "schnaps.fun", path = "/")
    }

    fun ResponseCookies.removeJWTToken() {
        append("token", "", httpOnly = true, secure = true, domain = "schnaps.fun", path = "/")
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .build())

            validate { jwtCredential ->
                JWTPrincipal(jwtCredential.payload)
            }

            // Retrieve token from httponly cookie instead ot header to prevent XSS attacks
            authHeader {
                val token = it.request.cookies["token"]
                if (token.isNullOrEmpty()) return@authHeader null

                HttpAuthHeader.Single("Bearer", token)
            }
        }
    }


    /**
     * Routes
     */
    routing {

        route("/login") {
            for ((providerName, provider) in OAuth2Providers) {
                post("/$providerName") {
                    val authorizationCode = call.receiveOrNull<AuthorizationCodeBody>()?.code ?: return@post call.respond(HttpStatusCode.BadRequest)

                    // Get external info
                    val profile = kotlin.runCatching {
                        provider.getUserProfile(authorizationCode)
                    }.getOrElse {
                        it.printStackTrace()
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // Get user from info
                    val result = transaction {
                        ExternalUser.find {
                            ExternalUsers.providerUserId eq profile.id and (ExternalUsers.provider eq ExternalUsers.Provider.valueOf(providerName.uppercase()))
                        }.toList()
                    }

                    // If user doesn't exist, create it and log in
                    if (result.isEmpty()) {
                        val user = transaction {
                            User.new {
                                type = Users.Type.EXTERNAL
                            }
                        }
                        val externalUser = transaction {
                            ExternalUser.new(user.id.value) {
                                this.provider       = ExternalUsers.Provider.valueOf(providerName.uppercase())
                                this.providerUserId = profile.id
                                this.username       = profile.username
                            }
                        }
                        transaction {
                            call.response.cookies.addJWTToken(user.id.value, externalUser.username, user.administrator)
                        }

                        return@post call.respond(HttpStatusCode.Created)
                    }

                    // Else, get user and log in
                    val externalUser = result.single()
                    call.response.cookies.addJWTToken(externalUser.id.value, externalUser.username, UserService.isAdministrator(externalUser.id.value)!!)
                    call.respond(HttpStatusCode.OK)
                }
            }

            // Authenticate local user
            post("/local") {
                val login = call.receiveOrNull<UserLogin>() ?: return@post call.respond(HttpStatusCode.BadRequest)
                println(login)

                val localUser = try {
                    transaction {
                        LocalUser.find { LocalUsers.username eq login.username }.single()
                    }
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.Unauthorized, "Ce compte n'existe pas.")
                }
                if (checkHash(login.password, localUser.password)) {
                    call.response.cookies.addJWTToken(localUser.id.value, localUser.username, UserService.isAdministrator(localUser.id.value)!!)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Mot de passe incorrect.")
                }
            }
        }

        // Create local user and authenticate them
        post("/signup") {
            val body = call.receiveOrNull<SignupRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val violations = DataValidator.validateSignupRequest(body)

            if (violations.isNotEmpty()) {
                return@post call.respond(HttpStatusCode.BadRequest, violations)
            }

            val user = transaction {
                User.new {
                    type = Users.Type.LOCAL
                }
            }
            val localUser = transaction {
                LocalUser.new(user.id.value) {
                    username = body.username
                    password = getHash(body.password)
                }
            }
            transaction {
                call.response.cookies.addJWTToken(user.id.value, localUser.username, user.administrator)
            }
            call.respond(HttpStatusCode.Created)
        }

        authenticate("auth-jwt") {

            // Generate recovery token
            get("/generateRecoveryToken") {
                val principal = call.principal<JWTPrincipal>()
                val id = principal!!.subject?.toInt() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val user = transaction {
                    User.findById(id)
                } ?: return@get call.respond(HttpStatusCode.InternalServerError)
                if (user.type != Users.Type.LOCAL) return@get call.respond(HttpStatusCode.BadRequest)

                val recoveryToken = UUID.randomUUID().toString()
                val hash = getHash(recoveryToken)
                //TODO
            }

            // Disconnect
            delete("/disconnect") {
                call.response.cookies.removeJWTToken()
                call.respond(HttpStatusCode.OK)
            }
        }


        // Refresh token with every request
        intercept(ApplicationCallPipeline.Features) {
            if (context.request.cookies["token"].isNullOrEmpty()) return@intercept
            val payload = context.authentication.principal<JWTPrincipal>()?.payload ?: return@intercept
            val expireTime = payload.expiresAt.toInstant()

            if (expireTime.isBefore(Instant.now())) return@intercept

            val id = payload.subject.toInt()
            val username = payload.getClaim("username").asString()
            val administrator = payload.getClaim("administrator").asBoolean()

            context.response.cookies.addJWTToken(id, username, administrator)
        }
    }
}

/**
 * Generates the BCrypt hash for a given string
 * @return A byte array of size 60
 */
fun getHash(string: String): String = BCrypt.hashpw(string, BCrypt.gensalt())

fun checkHash(string: String, hash: String) = BCrypt.checkpw(string, hash)
