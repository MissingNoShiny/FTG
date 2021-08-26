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

    fun generateJWTToken(id: Int, username:String) = JWT.create()
        .withAudience(audience)
        .withSubject(id.toString())
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + duration))
        .sign(Algorithm.HMAC256(secret))

    fun ResponseCookies.addJWTToken(id: Int, username: String) {
        val token = generateJWTToken(id, username)
        append("token", token, httpOnly = true, secure = false)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JWT
                .require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .build())

            validate { jwtCredential ->
                if (User.findById(jwtCredential.payload.subject.toInt()) != null) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
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
                    val authorizationCode = call.receiveOrNull<String>() ?: return@post call.respond(HttpStatusCode.BadRequest)

                    // Get external info
                    val profile = kotlin.runCatching {
                        provider.getUserProfile(authorizationCode)
                    }.getOrElse {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // Get user from info
                    val result = ExternalUser.find {
                        ExternalUsers.providerUserId eq profile.id and (ExternalUsers.provider eq ExternalUsers.Provider.valueOf(providerName))
                    }

                    // If user doesn't exist, create it and log in
                    if (result.empty()) {
                        val user = transaction {
                            val user = User.new {
                                type = Users.Type.EXTERNAL
                            }
                            ExternalUser.new(user.id.value) {
                                this.provider       = ExternalUsers.Provider.valueOf(providerName)
                                this.providerUserId = profile.id
                                this.username       = profile.username
                            }
                        }

                        call.response.cookies.addJWTToken(user.id.value, user.username)
                        return@post call.respond(HttpStatusCode.Created)
                    }

                    // Else, get user and log in
                    val externalUser = result.single()
                    call.response.cookies.addJWTToken(externalUser.id.value, externalUser.username)
                    call.respond(HttpStatusCode.OK)
                }
            }

            // Authenticate local user
            post("/local") {
                val login = call.receiveOrNull<UserLogin>() ?: return@post call.respond(HttpStatusCode.BadRequest)
                
                val localUser = try {
                    LocalUser.find { LocalUsers.username eq login.username }.single()
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }

                if (checkPassword(localUser, login.password)) {
                    call.response.cookies.addJWTToken(localUser.id.value, localUser.username)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            // Create local user and authenticate it
            post("/signup") {
                val body = call.receiveOrNull<SignupRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)
                val violations = DataValidator.validateSignupRequest(body)

                if (violations.isNotEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, violations)
                }

                val user = transaction {
                    val user = User.new {
                        type = Users.Type.LOCAL
                    }
                    LocalUser.new(user.id.value) {
                        username = body.username
                        password = getHash(body.password)
                    }
                }

                call.response.cookies.addJWTToken(user.id.value, user.username)
                call.respond(HttpStatusCode.Created)
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

            context.response.cookies.addJWTToken(id, username)
        }
    }
}

/**
 * Generates the BCrypt hash for a given string
 * @return A byte array of size 40
 */
private fun getHash(string: String) = BCrypt.hashpw(string, BCrypt.gensalt()).toByteArray()

private fun checkPassword(user: LocalUser, password: String) = user.password contentEquals getHash(password)
