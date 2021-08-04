package xyz.missingnoshiny.ftg.server.api

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(val username: String, val password: String)
