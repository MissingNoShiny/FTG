package xyz.missingnoshiny.ftg.server

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

@Serializable
class Node {
    var rooms: MutableMap<String, List<Int>> = mutableMapOf()
    var apiAddress: String? = null
    var lastHeartBeat: Instant = Instant.DISTANT_PAST

    /**
     *
     */
    val isReady: Boolean
        get() = apiAddress != null

    @OptIn(ExperimentalTime::class)
    val isAlive: Boolean
        get() = lastHeartBeat.minus(Clock.System.now()).inWholeSeconds < 10

    val weight: Int
        get() = rooms.size
}