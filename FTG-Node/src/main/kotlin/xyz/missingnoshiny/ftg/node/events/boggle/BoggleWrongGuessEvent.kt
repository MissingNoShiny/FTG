package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class BoggleWrongGuessEvent(val guess: String): OutgoingEvent()