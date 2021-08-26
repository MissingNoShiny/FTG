package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class BoggleCorrectGuessEvent(val guess: String, val score: Int): OutgoingEvent()