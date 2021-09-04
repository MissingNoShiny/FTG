package xyz.missingnoshiny.ftg.node.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.auth.*

fun Application.configureAuthentication() {

    /**
     * JWT config
     */
    val secret = environment.config.property("jwt.secret").getString()
    val audience = environment.config.property("jwt.audience").getString()

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .build()
            )

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
}