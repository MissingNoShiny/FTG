package xyz.missingnoshiny.ftg.node.games

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(val authorId: Int, val message: String, val timestamp: Instant)