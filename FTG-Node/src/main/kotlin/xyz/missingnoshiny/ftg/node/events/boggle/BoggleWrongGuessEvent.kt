package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.node.games.boggle.GuessStatus

@Serializable
data class BoggleWrongGuessEvent(val guess: String, val status: GuessStatus): OutgoingEvent()