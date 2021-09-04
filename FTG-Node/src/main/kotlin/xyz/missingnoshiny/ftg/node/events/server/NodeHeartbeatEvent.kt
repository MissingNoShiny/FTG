package xyz.missingnoshiny.ftg.node.events.server

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.core.RoomSerializable

@Serializable
data class NodeHeartbeatEvent(val rooms: Map<String, RoomSerializable>): OutgoingEvent()