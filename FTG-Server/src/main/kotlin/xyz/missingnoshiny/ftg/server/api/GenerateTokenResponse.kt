package xyz.missingnoshiny.ftg.server.api

import kotlinx.serialization.Serializable

@Serializable
data class GenerateTokenResponse(val token: String)