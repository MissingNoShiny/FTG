package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.node.games.User

@Serializable
data class ChatMessageEvent(val senderId: Int, val sederUsername: String, val message: String): OutgoingEvent()