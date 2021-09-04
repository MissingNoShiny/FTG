package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.UserSerializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class RemovePlayerEvent(val player: UserSerializable): OutgoingEvent()