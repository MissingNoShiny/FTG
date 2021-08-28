package xyz.missingnoshiny.ftg.server.plugins.auth

import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

val appConfig = HoconApplicationConfig(ConfigFactory.load())

@Serializable
data class DiscordUser(val id: String, val username: String, val avatar: String)
@Serializable
data class DiscordResponse(val user: DiscordUser)

@Serializable
data class TwitchUser(val id: String, val display_name: String, val profile_image_url: String)
@Serializable
data class TwitchResponse(val data: List<TwitchUser>)

val OAuth2Providers = listOf(
    OAuth2Provider(
        name = "discord",
        accessTokenUrl = "https://discordapp.com/api/oauth2/token",
        accessTokenRequestBody = true,
        redirectUrl = "https://ftg.schnaps.fun/login/discord",
        profileUrl = "https://discordapp.com/api/oauth2/@me",
        clientId = appConfig.property("oauth.discord.id").getString(),
        clientSecret = appConfig.property("oauth.discord.secret").getString(),
        profileResponseReader = {
            val discordResponse = Json { ignoreUnknownKeys = true }.decodeFromString<DiscordResponse>(it)
            val profilePictureUrl = "https://cdn.discordapp.com/avatars/${discordResponse.user.id}/${discordResponse.user.avatar}.jpg"
            OAuth2Provider.Profile(discordResponse.user.id, discordResponse.user.username, profilePictureUrl)
        }
    ),
    OAuth2Provider(
        name = "twitch",
        accessTokenUrl = "https://id.twitch.tv/oauth2/token",
        accessTokenRequestBody = false,
        redirectUrl = "https://ftg.schnaps.fun/login/twitch",
        profileUrl = "https://api.twitch.tv/helix/users",
        clientId = appConfig.property("oauth.twitch.id").getString(),
        clientSecret = appConfig.property("oauth.twitch.secret").getString(),
        profileResponseReader = {
            val twitchResponse = Json { ignoreUnknownKeys = true }.decodeFromString<TwitchResponse>(it)
            val twitchUser = twitchResponse.data.single()
            OAuth2Provider.Profile(twitchUser.id, twitchUser.display_name, twitchUser.profile_image_url)
        }
    )
).associateBy { it.name }