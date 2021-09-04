package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.node.games.boggle.Guess

@Serializable
data class BoggleStopEvent(val solutions: List<Guess>): OutgoingEvent()