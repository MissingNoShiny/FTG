package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class BoggleUpdateScoreEvent(val playerId: Int, val newScore: Int): OutgoingEvent()