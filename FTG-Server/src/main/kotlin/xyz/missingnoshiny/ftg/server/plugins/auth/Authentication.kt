package xyz.missingnoshiny.ftg.server.plugins.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
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

fun Application.configureAuthentication() {

    /**
     * JWT
     */
    val realm = environment.config.property("jwt.realm").getString()
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val duration = environment.config.property("jwt.duration_ms").getString().toInt()
    fun generateJWTToken(userId: Int) = JWT.create()
        .withAudience(audience)
        .withSubject(userId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + duration))
        .sign(Algorithm.HMAC256(secret))

    install(Authentication) {
        jwt {
            this.realm = realm
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
        }
    }

    routing {

        route("/login") {
            for ((providerName, provider) in OAuth2Providers) {
                get("/$providerName") {
                    val authorizationCode = ""
                    val profile = provider.getUserProfile(authorizationCode)

                    if (ExternalUser.find {
                            ExternalUsers.providerUserId eq profile.id and (ExternalUsers.provider eq ExternalUsers.Provider.valueOf(providerName))
                    }.empty()) {
                        //TODO: Create user
                    } else {
                        //TODO: Login
                    }
                }
            }

            get("/local") {
                val login = call.receiveOrNull<UserLogin>() ?: return@get call.respond(HttpStatusCode.BadRequest)
                
                val localUser = try {
                    LocalUser.find { LocalUsers.username eq login.username }.single()
                } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.Unauthorized)
                }

                if (checkPassword(localUser, login.password)) {
                    call.respond(HttpStatusCode.OK, hashMapOf("token" to generateJWTToken(localUser.id.value)))
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            // New local user
            post("/signup") {
                val body = call.receiveOrNull<SignupRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)
                val violations = DataValidator.validateSignupRequest(body)

                if (violations.isNotEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, violations)
                }

                transaction {
                    val user = User.new {
                        type = Users.Type.LOCAL
                    }
                    LocalUser.new(user.id.value) {
                        username = body.username
                        password = getHash(body.password)
                    }
                }
                call.respond(HttpStatusCode.Created)
            }
        }


        // Refresh token with every request
        intercept(ApplicationCallPipeline.Call) {
            if (context.request.cookies["JWT_TOKEN"].isNullOrEmpty()) return@intercept

            val payload = context.authentication.principal<JWTPrincipal>()?.payload ?: return@intercept
            val expireTime = payload.expiresAt.toInstant()

            if (expireTime.isBefore(Instant.now())) return@intercept

            //TODO: Refresh token
        }
    }
}

fun getHash(password: String) = BCrypt.hashpw(password, BCrypt.gensalt()).toByteArray()

fun checkPassword(user: LocalUser, password: String) = user.password contentEquals getHash(password)

