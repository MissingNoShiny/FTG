package xyz.missingnoshiny.ftg.server.plugins.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class OAuth2Provider(
    val name: String,
    private val accessTokenUrl: String,
    private val accessTokenRequestBody: Boolean,
    private val redirectUrl: String,
    private val profileUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val profileResponseReader: (String) -> Profile
    ) {

    data class Profile(val id: String, val username: String, val profilePictureUrl: String)

    @Serializable
    private data class AccessTokenResponse(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Int,
        val token_type: String
    )

    private val httpClient = HttpClient()

    suspend fun getUserProfile(authorizationCode: String): Profile {
        val accessToken = getAccessToken(authorizationCode)

        val response: HttpResponse = httpClient.get(profileUrl) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
                append("Client-Id", clientId)
            }
        }
        return profileResponseReader(response.receive())
    }

    private suspend fun getAccessToken(authorizationCode: String): String {

        val response: HttpResponse = if (accessTokenRequestBody) {
            httpClient.submitForm(
                url = accessTokenUrl,
                formParameters = Parameters.build {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("code", authorizationCode)
                    append("grant_type", "authorization_code")
                    append("redirect_uri", redirectUrl)
                }
            )
        } else {
            httpClient.post("$accessTokenUrl?" + listOf(
                "client_id"     to clientId,
                "client_secret" to clientSecret,
                "code"          to authorizationCode,
                "grant_type"    to "authorization_code",
                "redirect_uri"  to redirectUrl
            ).formUrlEncode())
        }


        val responseData = Json { ignoreUnknownKeys = true }.decodeFromString<AccessTokenResponse>(response.receive())

        return responseData.access_token
    }
}