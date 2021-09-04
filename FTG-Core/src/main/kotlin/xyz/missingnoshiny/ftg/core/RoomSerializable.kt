package xyz.missingnoshiny.ftg.core

import kotlinx.serialization.Serializable

@Serializable
data class RoomSerializable(val id: String, val users: List<UserSerializable>, val isPublic: Boolean, val state: RoomState)