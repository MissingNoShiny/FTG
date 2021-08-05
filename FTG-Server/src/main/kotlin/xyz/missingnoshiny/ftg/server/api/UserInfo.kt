package xyz.missingnoshiny.ftg.server.api

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.server.db.Users

@Serializable
data class UserInfo(val username: String, val type: Users.Type)