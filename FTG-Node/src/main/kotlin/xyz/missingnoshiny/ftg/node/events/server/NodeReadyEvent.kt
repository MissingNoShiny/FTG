package xyz.missingnoshiny.ftg.node.events.server

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class NodeReadyEvent(val apiAddress: String): OutgoingEvent()