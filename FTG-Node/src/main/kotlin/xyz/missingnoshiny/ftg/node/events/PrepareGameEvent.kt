package xyz.missingnoshiny.ftg.node.events

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class PrepareGameEvent(val gameType: String = "Boggle"): OutgoingEvent()