package xyz.missingnoshiny.ftg.core

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomBody(val isPublic: Boolean)