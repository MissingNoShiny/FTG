package xyz.missingnoshiny.ftg.server.plugins.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserLogin(val username: String, val password: String)