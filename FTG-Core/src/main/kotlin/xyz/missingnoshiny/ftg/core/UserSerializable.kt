package xyz.missingnoshiny.ftg.core

import kotlinx.serialization.Serializable

@Serializable
data class UserSerializable(val id: Int, val username: String, val administrator: Boolean)