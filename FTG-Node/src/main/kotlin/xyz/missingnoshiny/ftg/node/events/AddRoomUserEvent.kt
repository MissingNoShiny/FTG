package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.core.UserSerializable

@Serializable
data class AddRoomUserEvent(val user: UserSerializable, val userList: List<UserSerializable>): OutgoingEvent()