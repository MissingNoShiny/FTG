package xyz.missingnoshiny.ftg.server.api

import kotlinx.serialization.Serializable

@Serializable
data class AuthorizationCodeBody(val code: String)