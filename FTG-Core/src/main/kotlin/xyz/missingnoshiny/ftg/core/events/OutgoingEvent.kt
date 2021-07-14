package xyz.missingnoshiny.ftg.core.events

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
abstract class OutgoingEvent {
    val timestamp = Clock.System.now()
}