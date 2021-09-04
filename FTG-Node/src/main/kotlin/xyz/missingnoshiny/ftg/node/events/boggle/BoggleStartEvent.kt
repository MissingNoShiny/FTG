package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

@Serializable
data class BoggleStartEvent(val grid: List<List<Char>>, val maxScore: Int, val wordCount: Int, val playing: Boolean): OutgoingEvent()