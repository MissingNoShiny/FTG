package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.node.serializable.ChatMessage

@Serializable
data class ChatMessageEvent(val chatMessage: ChatMessage): OutgoingEvent()