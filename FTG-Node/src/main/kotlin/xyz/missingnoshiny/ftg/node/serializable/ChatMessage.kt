package xyz.missingnoshiny.ftg.node.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.UserSerializable

@Serializable
data class ChatMessage(val author: UserSerializable, val message: String, val timestamp: Instant)